package com.eve.ticketing.app.seat.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeatReserveDto {

    long eventId;

    long maxTicketAmount;
}
