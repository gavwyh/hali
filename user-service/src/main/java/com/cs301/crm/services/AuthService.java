package com.cs301.crm.services;

import com.cs301.crm.dtos.requests.LoginRequestDTO;
import com.cs301.crm.dtos.requests.OtpVerificationDTO;
import com.cs301.crm.dtos.requests.ResendOtpRequestDTO;
import com.cs301.crm.dtos.responses.GenericResponseDTO;
import com.cs301.crm.dtos.responses.RefreshLoginResponseDTO;
import com.cs301.crm.models.UserEntity;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {
    GenericResponseDTO login(LoginRequestDTO loginRequestDTO);
    GenericResponseDTO verifyOtp(OtpVerificationDTO otpVerificationDTO);
    GenericResponseDTO resendOtp(ResendOtpRequestDTO otpRequestDTO);
    String generateAccessToken(UserDetails userDetails);
    String getJwkSet();
    RefreshLoginResponseDTO buildUserInformationResponse(UserEntity userEntity);
}