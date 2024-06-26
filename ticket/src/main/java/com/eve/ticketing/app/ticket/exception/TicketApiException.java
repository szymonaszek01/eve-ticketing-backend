package com.eve.ticketing.app.ticket.exception;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketApiException {

    private Integer status;

    private String message;

    private List<Error> errors;
}
