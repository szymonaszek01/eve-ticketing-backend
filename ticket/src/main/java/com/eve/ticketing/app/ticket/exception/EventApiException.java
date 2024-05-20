package com.eve.ticketing.app.ticket.exception;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventApiException {

    private Integer status;

    private String message;

    private List<Error> errors;
}
