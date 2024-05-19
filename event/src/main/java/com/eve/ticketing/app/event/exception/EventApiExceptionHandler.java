package com.eve.ticketing.app.event.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class EventApiExceptionHandler {

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest httpServletRequest) {
        List<Error> errors = e.getConstraintViolations().stream()
                .map(constraintViolation -> Error.builder()
                        .method(httpServletRequest.getMethod())
                        .field(constraintViolation.getPropertyPath().toString())
                        .value(constraintViolation.getInvalidValue())
                        .message(constraintViolation.getMessage()).build())
                .toList();

        EventApiException eventApiException = EventApiException.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Entity \"Event\" can not be " + ("PUT".equalsIgnoreCase(httpServletRequest.getMethod()) ? "updated" : "created") + " due to constraint violation")
                .errors(errors)
                .build();

        return new ResponseEntity<>(eventApiException, new HttpHeaders(), eventApiException.getStatus());
    }

    @ExceptionHandler({EventProcessingException.class})
    public ResponseEntity<Object> handleEventProcessingException(EventProcessingException e) {
        List<Error> errors = List.of(e.getError());

        EventApiException eventApiException = EventApiException.builder()
                .status(e.getStatus().value())
                .message("Entity \"Event\" can not be processed, because an error occurred")
                .errors(errors)
                .build();

        return new ResponseEntity<>(eventApiException, new HttpHeaders(), eventApiException.getStatus());
    }
}
