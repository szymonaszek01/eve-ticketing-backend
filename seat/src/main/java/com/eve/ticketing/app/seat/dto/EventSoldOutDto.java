package com.eve.ticketing.app.seat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventSoldOutDto {

    @NotNull
    private Long eventId;

    @NonNull
    private Boolean isSoldOut;
}
