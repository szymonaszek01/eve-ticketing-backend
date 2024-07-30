package com.eve.ticketing.app.pdf.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class PdfProcessingException extends RuntimeException {

    private HttpStatus status;

    private Error error;

    public PdfProcessingException(HttpStatus status, Error error) {
        this.status = status;
        this.error = error;
    }
}
