package com.cs301.crm.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CookieUtil {

    private final List<String> cookiePaths = List.of("/api/v1/auth/refresh", "/api/v1/auth/logout");

    @Value("${cookie.httpsEnabled}")
    private boolean httpsEnabled;

    @Value("${jwt.refresh.duration}")
    private long refreshTokenDurationInSeconds;

    @Value("${jwt.access.duration}")
    private long accessTokenDurationInSeconds;

    public List<ResponseCookie> buildRefreshToken(String attributeValue) {

        List<ResponseCookie> cookies = new ArrayList<>();
        for (String path : cookiePaths) {
            cookies.add(ResponseCookie.from("refreshToken", attributeValue)
                    .httpOnly(true)
                    .secure(httpsEnabled)
                    .path(path)
//                    .sameSite("None")
                    .maxAge(refreshTokenDurationInSeconds)
                    .build()
            );
        }
        return cookies;
    }

    public ResponseCookie buildAccessToken(String attributeValue) {
        return ResponseCookie.from("accessToken", attributeValue)
                .httpOnly(true)
                .secure(httpsEnabled)
                .path("/")
//                .sameSite("None")
                .maxAge(accessTokenDurationInSeconds)
                .build();
    }
}