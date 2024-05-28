package com.eve.ticketing.app.ticket.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
@Slf4j
public class TicketApiExceptionHandler {

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest httpServletRequest) {
        List<Error> errors = e.getConstraintViolations().stream()
                .map(constraintViolation -> Error.builder()
                        .method(httpServletRequest.getMethod())
                        .field(toSnake(constraintViolation.getPropertyPath().toString()))
                        .value(constraintViolation.getInvalidValue())
                        .description(constraintViolation.getMessage()).build())
                .toList();

        errors.forEach(error -> log.error(error.toString()));

        TicketApiException ticketApiException = TicketApiException.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("entity ticket can not be " + ("PUT".equalsIgnoreCase(httpServletRequest.getMethod()) ? "updated" : "created") + " due to constraint violation")
                .errors(errors)
                .build();

        return new ResponseEntity<>(ticketApiException, new HttpHeaders(), ticketApiException.getStatus());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest httpServletRequest) {
        List<Error> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> Error.builder()
                        .method(httpServletRequest.getMethod())
                        .field(toSnake(fieldError.getField()))
                        .value(fieldError.getRejectedValue())
                        .description(fieldError.getDefaultMessage()).build())
                .toList();

        errors.forEach(error -> log.error(error.toString()));

        TicketApiException ticketApiException = TicketApiException.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("entity ticket can not be " + ("PUT".equalsIgnoreCase(httpServletRequest.getMethod()) ? "updated" : "created") + " due to field errors")
                .errors(errors)
                .build();

        return new ResponseEntity<>(ticketApiException, new HttpHeaders(), ticketApiException.getStatus());
    }

    @ExceptionHandler({TicketProcessingException.class})
    public ResponseEntity<Object> handleEventProcessingException(TicketProcessingException e) {
        List<Error> errors = List.of(e.getError());

        TicketApiException ticketApiException = TicketApiException.builder()
                .status(e.getStatus().value())
                .message("entity ticket can not be processed, because an error occurred")
                .errors(errors)
                .build();

        return new ResponseEntity<>(ticketApiException, new HttpHeaders(), ticketApiException.getStatus());
    }

    private String toSnake(String str) {
        StringBuilder result = new StringBuilder();
        char c = str.charAt(0);
        result.append(Character.toLowerCase(c));

        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }
}
