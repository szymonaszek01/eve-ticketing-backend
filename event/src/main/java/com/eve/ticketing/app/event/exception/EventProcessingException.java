package com.eve.ticketing.app.event.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class EventProcessingException extends RuntimeException {

    private HttpStatus status;

    private Error error;

    public EventProcessingException(HttpStatus status, Error error) {
        this.status = status;
        this.error = error;
    }
}
