package com.cs301.crm.utils;

import com.cs301.crm.exceptions.InvalidOtpException;
import com.cs301.crm.models.UserEntity;
import com.cs301.crm.producers.KafkaProducer;
import com.cs301.shared.protobuf.Otp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Value("${otp.expiration}")
    private Long otpExpiration;

    private Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaProducer kafkaProducer;

    // TTL constants
    private static final String OTP_KEY = "otp:email:";
    private static final String PENDING_ACTION_KEY = "pending:email:";

    public RedisUtil(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, KafkaProducer kafkaProducer) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.kafkaProducer = kafkaProducer;
    }

    // Generate and store OTP
    public void generateOtp(String email) {
        final int otp = PasswordUtil.generateOtpValue();

        // Store OTP with key: otp:email:{email} and otp value in String for TTL minutes
        redisTemplate.opsForValue().set(OTP_KEY + email, String.valueOf(otp), otpExpiration, TimeUnit.SECONDS);
        logger.info("{} otp stored in Redis", otp);

        Otp otpMessage = Otp.newBuilder()
                .setUserEmail(email)
                .setOtp(otp)
                .setTimestamp(Instant.now().toString())
                .build();

        kafkaProducer.produceMessage(otpMessage);
        logger.info("Otp message sent to Kafka");
    }

    // Overload for dangerous operations (create/update actions on users) with metadata
    public void generateOtp(String email, UserEntity userEntity) throws JsonProcessingException {
        this.generateOtp(email);

        // stringify the userentity
        String userJson = objectMapper.writeValueAsString(userEntity);
        redisTemplate.opsForValue().set(PENDING_ACTION_KEY + email, userJson, otpExpiration, TimeUnit.SECONDS);
        logger.info("Pending action stored in Redis");
    }

    // Validate OTP
    public boolean verifyOtp(String email, String providedOtp) {
        String storedOtp = redisTemplate.opsForValue().get(OTP_KEY + email);
        logger.info("got {} for {}{}", storedOtp, OTP_KEY, email);
        return storedOtp == null || !storedOtp.equals(providedOtp);
    }

    // Retrieve frozen user entity after successful OTP validation
    public UserEntity retrievePendingUser(String email) throws JsonProcessingException {
        String userJson = redisTemplate.opsForValue().get(PENDING_ACTION_KEY + email);

        if (userJson == null) {
            throw new InvalidOtpException("OTP validated on a user that does not exist.");
        }
        return objectMapper.readValue(userJson, UserEntity.class);
    }

    // Cleanup after successful verification
    public void cleanupAfterSuccessfulVerification(String email) {
        redisTemplate.delete(OTP_KEY + email);
        redisTemplate.delete(PENDING_ACTION_KEY + email);
        logger.info("Successful verification for {}, cleaning up Redis entries", email);
    }

    // Invalidate existing OTPs before resending
    public void invalidateExistingOtps(String email) {
        redisTemplate.delete(OTP_KEY + email);
        logger.info("Invalidated Redis entries for {}", email);
    }
}
