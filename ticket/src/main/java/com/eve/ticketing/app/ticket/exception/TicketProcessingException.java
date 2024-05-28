package com.eve.ticketing.app.ticket.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class TicketProcessingException extends RuntimeException {

    private HttpStatus status;

    private Error error;

    public TicketProcessingException(HttpStatus status, Error error) {
        this.status = status;
        this.error = error;
    }
}
