package com.cs301.crm.exceptions;

import org.springframework.http.HttpStatus;
import java.time.ZonedDateTime;

public record ErrorResponse(
        boolean success,
        String message,

        HttpStatus httpStatus,

        ZonedDateTime timestamp
) {

}
