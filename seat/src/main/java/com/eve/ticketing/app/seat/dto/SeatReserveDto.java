package com.eve.ticketing.app.seat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeatReserveDto {

    @NotNull
    private Long eventId;

    @NotNull
    private Long maxTicketAmount;
}
