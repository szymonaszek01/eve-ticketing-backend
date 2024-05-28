package com.eve.ticketing.app.authuser.exception;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserApiException {

    private Integer status;

    private String message;

    private List<Error> errors;
}
