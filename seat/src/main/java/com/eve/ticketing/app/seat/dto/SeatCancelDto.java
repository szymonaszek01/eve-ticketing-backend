package com.eve.ticketing.app.seat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeatCancelDto {

    @NotNull
    private Long seatId;

    @NotNull
    private Boolean isSoldOut;
}
