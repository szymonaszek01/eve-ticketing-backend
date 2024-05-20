package com.eve.ticketing.app.smsnotification.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
@Slf4j
public class EventApiExceptionHandler {

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

        EventApiException eventApiException = EventApiException.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("entity sms-notification can not be " + ("PUT".equalsIgnoreCase(httpServletRequest.getMethod()) ? "updated" : "created") + " due to constraint violation")
                .errors(errors)
                .build();

        return new ResponseEntity<>(eventApiException, new HttpHeaders(), eventApiException.getStatus());
    }

    @ExceptionHandler({SmsNotificationProcessingException.class})
    public ResponseEntity<Object> handleEventProcessingException(SmsNotificationProcessingException e) {
        List<Error> errors = List.of(e.getError());

        EventApiException eventApiException = EventApiException.builder()
                .status(e.getStatus().value())
                .message("entity sms-notification can not be processed, because an error occurred")
                .errors(errors)
                .build();

        return new ResponseEntity<>(eventApiException, new HttpHeaders(), eventApiException.getStatus());
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
