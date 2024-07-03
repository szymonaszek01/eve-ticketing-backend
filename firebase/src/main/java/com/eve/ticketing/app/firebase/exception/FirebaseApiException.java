package com.eve.ticketing.app.firebase.exception;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FirebaseApiException {

    private Integer status;

    private String message;

    private List<Error> errors;
}
