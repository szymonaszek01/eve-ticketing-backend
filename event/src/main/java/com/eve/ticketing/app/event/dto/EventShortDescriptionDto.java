package com.eve.ticketing.app.event.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventShortDescriptionDto {

    private long eventId;

    private String name;

    private long maxTicketAmount;

    private boolean isSoldOut;

    private BigDecimal unitPrice;

    private BigDecimal childrenDiscount;

    private BigDecimal studentsDiscount;

    private Date startAt;

    private Date endAt;

    private boolean isWithoutSeats;
}
