package com.cs301.crm.exceptions.handlers;

import com.cs301.crm.exceptions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handlePoorInputs(MethodArgumentNotValidException e) {
        StringBuilder sb = new StringBuilder();

        e.getBindingResult().getAllErrors().forEach((error) -> {
            String errorMessage = error.getDefaultMessage();
            sb.append(errorMessage).append(", ");
        });
        return new ResponseEntity<>(
                new ErrorResponse(false,
                        sb.substring(0, sb.length() - 2),
                        HttpStatus.BAD_REQUEST,
                        ZonedDateTime.now()
                ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleUsernameDoesNotExist(UsernameNotFoundException e) {
        return new ResponseEntity<>(
                new ErrorResponse(false,"Email " + e.getMessage() + " not found",
                        HttpStatus.BAD_REQUEST,
                        ZonedDateTime.now()
                ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {InternalAuthenticationServiceException.class})
    public ResponseEntity<ErrorResponse> handleEmailNotFound() {
        return new ResponseEntity<>(
                new ErrorResponse(false, "You do not have an account, contact a root user or admin to get an account",
                        HttpStatus.UNAUTHORIZED,
                        ZonedDateTime.now()
                ), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleInvalidCredentials() {
        return new ResponseEntity<>(
                new ErrorResponse(false, "Ensure you have typed your email/password correctly",
                        HttpStatus.UNAUTHORIZED,
                        ZonedDateTime.now()
                ), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {InvalidUserCredentials.class})
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidUserCredentials e) {
        return new ResponseEntity<>(
                new ErrorResponse(false, e.getMessage(),
                        HttpStatus.UNAUTHORIZED,
                        ZonedDateTime.now()
                ), HttpStatus.UNAUTHORIZED);
    }

    // This is for if the OTP value is wrong, but OTP correct number does exist in the cache
    @ExceptionHandler(value = {InvalidOtpException.class})
    public ResponseEntity<ErrorResponse> handleInvalidOtpSubmission(Exception e) {
        return new ResponseEntity<>(
                new ErrorResponse(false,e.getMessage(),
                        HttpStatus.UNAUTHORIZED,
                        ZonedDateTime.now()
                ), HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(value = {HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBlankRequests() {

        return new ResponseEntity<>(
                new ErrorResponse(false,"You are missing inputs in your request, please follow our API documentation",
                        HttpStatus.BAD_REQUEST,
                        ZonedDateTime.now()
                ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {Exception.class, JwtCreationException.class, JsonProcessingException.class})
    public ResponseEntity<ErrorResponse> handleException() {
        return new ResponseEntity<>(
                new ErrorResponse(false,"Something went wrong on our end.",
                        HttpStatus.SERVICE_UNAVAILABLE,
                        ZonedDateTime.now()
                ), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(value = {InvalidTokenException.class})
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidTokenException e) {
        return new ResponseEntity<>(
                new ErrorResponse(false, e.getMessage(),
                        HttpStatus.UNAUTHORIZED,
                        ZonedDateTime.now()
                ), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {DisabledException.class})
    public ResponseEntity<ErrorResponse> handleAccountDisabled(Exception e) {
        return new ResponseEntity<>(
                new ErrorResponse(false, e.getMessage(),
                        HttpStatus.UNAUTHORIZED,
                        ZonedDateTime.now()
                ), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {InvalidChangeException.class})
    public ResponseEntity<ErrorResponse> handleInvalidChangeToRoot(InvalidChangeException e) {
        return new ResponseEntity<>(
                new ErrorResponse(false, e.getMessage(),
                        HttpStatus.UNAUTHORIZED,
                        ZonedDateTime.now()
                ), HttpStatus.UNAUTHORIZED);
    }
}
