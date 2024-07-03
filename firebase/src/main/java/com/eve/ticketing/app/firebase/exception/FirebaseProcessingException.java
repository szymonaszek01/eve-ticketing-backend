package com.eve.ticketing.app.firebase.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class FirebaseProcessingException extends RuntimeException {

    private HttpStatus status;

    private Error error;

    public FirebaseProcessingException(HttpStatus status, Error error) {
        this.status = status;
        this.error = error;
    }
}
