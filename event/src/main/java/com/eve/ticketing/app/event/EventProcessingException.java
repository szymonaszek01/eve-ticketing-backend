package com.eve.ticketing.app.event;

public class EventProcessingException extends RuntimeException {
    public EventProcessingException(String message) {
        super(message);
    }
}
