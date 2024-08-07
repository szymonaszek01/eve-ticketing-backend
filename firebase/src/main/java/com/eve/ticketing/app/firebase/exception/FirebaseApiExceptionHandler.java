package com.eve.ticketing.app.firebase.exception;

import jakarta.servlet.http.HttpServletRequest;
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
public class FirebaseApiExceptionHandler {

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

        FirebaseApiException firebaseApiException = FirebaseApiException.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("file can not be " + ("POST".equalsIgnoreCase(httpServletRequest.getMethod()) ? "uploaded" : "downloaded") + " due to field errors")
                .errors(errors)
                .build();

        return new ResponseEntity<>(firebaseApiException, new HttpHeaders(), firebaseApiException.getStatus());
    }

    @ExceptionHandler({FirebaseProcessingException.class})
    public ResponseEntity<Object> handleFirebaseProcessingException(FirebaseProcessingException e) {
        List<Error> errors = List.of(e.getError());

        FirebaseApiException firebaseApiException = FirebaseApiException.builder()
                .status(e.getStatus().value())
                .message("entity firebase can not be processed, because an error occurred")
                .errors(errors)
                .build();

        return new ResponseEntity<>(firebaseApiException, new HttpHeaders(), firebaseApiException.getStatus());
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
