package com.eve.ticketing.app.smsnotification.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class SmsNotificationProcessingException extends RuntimeException {

    private HttpStatus status;

    private Error error;

    public SmsNotificationProcessingException(HttpStatus status, Error error) {
        this.status = status;
        this.error = error;
    }
}
