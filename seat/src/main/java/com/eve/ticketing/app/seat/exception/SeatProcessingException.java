package com.eve.ticketing.app.seat.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class SeatProcessingException extends RuntimeException {

    private HttpStatus status;

    private Error error;

    public SeatProcessingException(HttpStatus status, Error error) {
        this.status = status;
        this.error = error;
    }
}
