package com.cs301.crm.services.impl;

import com.cs301.crm.dtos.requests.*;
import com.cs301.crm.dtos.responses.GenericResponseDTO;
import com.cs301.crm.exceptions.InvalidChangeException;
import com.cs301.crm.exceptions.InvalidOtpException;
import com.cs301.crm.exceptions.InvalidUserCredentials;
import com.cs301.crm.mappers.UserEntityMapper;
import com.cs301.crm.models.OtpContext;
import com.cs301.crm.models.UserEntity;
import com.cs301.crm.models.UserRole;
import com.cs301.crm.producers.KafkaProducer;
import com.cs301.shared.protobuf.U2C;
import com.cs301.crm.repositories.UserRepository;
import com.cs301.crm.services.UserService;
import com.cs301.crm.utils.PasswordUtil;
import com.cs301.crm.utils.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducer kafkaProducer;
    private final RedisUtil redisUtil;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           KafkaProducer kafkaProducer,
                           RedisUtil redisUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaProducer = kafkaProducer;
        this.redisUtil = redisUtil;
    }

    @Override
    @Transactional
    public GenericResponseDTO getUsers() {
        return new GenericResponseDTO(
                true, userRepository.findAllUsers(), ZonedDateTime.now()
        );
    }

    @Override
    @Transactional
    public GenericResponseDTO getActiveAgents() {
        return new GenericResponseDTO(
                true, userRepository.findAllActiveAgents(), ZonedDateTime.now()
        );
    }

    @Override
    @Transactional
    public GenericResponseDTO createUser(CreateUserRequestDTO createUserRequestDTO) throws JsonProcessingException {
        // Create the new user
        String tempPassword = PasswordUtil.generatePassword();
        UserEntity userEntity = UserEntityMapper.INSTANCE.createUserRequestDTOtoUserEntity(createUserRequestDTO);
        userEntity.setPassword(tempPassword);
        logger.info("Generated password {}", tempPassword);

        // Send Otp to user and freeze actions
        this.enforceTwoFactorAuthentication(userEntity);

        return new GenericResponseDTO(
                true, "Please verify account creation with OTP sent to your email", ZonedDateTime.now()
        );
    }

    @Override
    @Transactional
    public GenericResponseDTO toggleEnable(DisableEnableRequestDTO disableEnableRequestDTO, boolean enable) throws JsonProcessingException {
        final String email = disableEnableRequestDTO.email();
        this.checkIfAccountIsRoot(email);

        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException(email)
        );

        userEntity.setEnabled(enable);
        this.enforceTwoFactorAuthentication(userEntity);

        return new GenericResponseDTO(
                true, "Please verify account update with OTP sent to your email", ZonedDateTime.now()
        );
    }

    @Override
    @Transactional
    public GenericResponseDTO verifyOtp(DangerousActionOtpVerificationDTO otpVerificationDTO) throws JsonProcessingException {
        if (redisUtil.verifyOtp(otpVerificationDTO.email(), otpVerificationDTO.oneTimePassword())) {
            throw new InvalidOtpException("OTP value is wrong. Please try again");
        }

        UserEntity userEntity = redisUtil.retrievePendingUser(otpVerificationDTO.email());
        logger.info("2FA passed, enabled action on {}", userEntity);

        String result = "";

        if (OtpContext.valueOf(otpVerificationDTO.otpContext().toUpperCase()) == OtpContext.CREATE) {
            // Send notification of account creation to new user
            U2C notificationMessage = U2C.newBuilder()
                    .setUserEmail(userEntity.getEmail())
                    .setUsername(userEntity.getFirstName())
                    .setTempPassword(userEntity.getPassword())
                    .setUserRole(userEntity.getUserRole().toString())
                    .build();

            kafkaProducer.produceMessage(notificationMessage);
            logger.info("Sent notification message to Kafka");

            userEntity.setEnabled(true);
            userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
            result = "user created successfully";
        } else if (OtpContext.valueOf(otpVerificationDTO.otpContext().toUpperCase()) == OtpContext.UPDATE) {
            result = "user updated successfully.";
        }

        userRepository.save(userEntity);
        redisUtil.cleanupAfterSuccessfulVerification(otpVerificationDTO.email());
        return new GenericResponseDTO(
                true, "2FA verification successful, " + result, ZonedDateTime.now()
        );
    }

    @Override
    @Transactional
    public GenericResponseDTO updateUser(UpdateUserRequestDTO updateUserRequestDTO) throws JsonProcessingException {
        final String email = updateUserRequestDTO.email();
        this.checkIfAccountIsRoot(email);

        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException(email)
        );
        userEntity.setFirstName(updateUserRequestDTO.firstName());
        userEntity.setLastName(updateUserRequestDTO.lastName());
        userEntity.setUserRole(UserRole.valueOf(updateUserRequestDTO.userRole()));

        this.enforceTwoFactorAuthentication(userEntity);

        return new GenericResponseDTO(
                true, "Please verify account update with OTP sent to your email", ZonedDateTime.now()
        );
    }

    @Override
    public GenericResponseDTO resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO) {
        UserEntity userEntity = userRepository.findByEmail(resetPasswordRequestDTO.email()).orElseThrow(
                () -> new UsernameNotFoundException(resetPasswordRequestDTO.email())
        );

        if (!passwordEncoder.matches(
                resetPasswordRequestDTO.oldPassword(), userEntity.getPassword())
        ) {
            throw new InvalidUserCredentials("Invalid old password");
        }

        userEntity.setPassword(passwordEncoder.encode(resetPasswordRequestDTO.newPassword()));

        return new GenericResponseDTO(
                true, "User password updated successfully", ZonedDateTime.now()
        );
    }

    private void enforceTwoFactorAuthentication(UserEntity frozenUserEntity) throws JsonProcessingException {
        // Get current user
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String actorId = jwt.getClaimAsString("sub");
        UserEntity actor = userRepository.findById(UUID.fromString(actorId))
                .orElseThrow(() -> new UsernameNotFoundException(actorId));

        redisUtil.generateOtp(actor.getEmail(), frozenUserEntity);
    }

    private void checkIfAccountIsRoot(String email) {
        if (email.equals("root@root.com")) {
            throw new InvalidChangeException("Not allowed to change information of root user.");
        }
    }
}