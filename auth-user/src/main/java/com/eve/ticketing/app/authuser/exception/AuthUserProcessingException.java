package com.eve.ticketing.app.authuser.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class AuthUserProcessingException extends RuntimeException {

    private HttpStatus status;

    private Error error;

    public AuthUserProcessingException(HttpStatus status, Error error) {
        this.status = status;
        this.error = error;
    }
}
