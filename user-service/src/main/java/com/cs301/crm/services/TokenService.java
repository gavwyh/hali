package com.cs301.crm.services;

import com.cs301.crm.dtos.responses.GenericResponseDTO;
import com.cs301.crm.models.RefreshToken;

import java.util.UUID;

public interface TokenService {
    String createRefreshToken(String email);
    RefreshToken validateRefreshToken(UUID refreshToken);
    GenericResponseDTO logout(UUID refreshToken);
}
