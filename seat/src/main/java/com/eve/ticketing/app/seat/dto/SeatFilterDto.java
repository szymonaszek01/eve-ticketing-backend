package com.eve.ticketing.app.seat.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeatFilterDto {

    private String sector;

    private Integer row;

    private Integer number;
}
