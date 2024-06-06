package com.eve.ticketing.app.seat.exception;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeatApiException {

        private Integer status;

        private String message;

        private List<Error> errors;
}
