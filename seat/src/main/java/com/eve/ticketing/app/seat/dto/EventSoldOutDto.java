package com.eve.ticketing.app.seat.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventSoldOutDto {

    private long eventId;

    private boolean isSoldOut;
}
