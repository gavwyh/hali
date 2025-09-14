package com.cs301.crm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;
import java.time.ZonedDateTime;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(value = {ConnectException.class})
    public ResponseEntity<ErrorResponse> handleConnectException() {
        return new ResponseEntity<>(
                new ErrorResponse(false,"Something went wrong on our end. Try again in a few moments.",
                        HttpStatus.SERVICE_UNAVAILABLE,
                        ZonedDateTime.now()
                ), HttpStatus.SERVICE_UNAVAILABLE);
    }

}
