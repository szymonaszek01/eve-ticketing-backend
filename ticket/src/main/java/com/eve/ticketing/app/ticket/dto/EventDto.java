package com.eve.ticketing.app.ticket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {

    private long id;

    private String name;

    @JsonProperty("max_ticket_amount")
    private long maxTicketAmount;

    @JsonProperty("is_sold_out")
    private boolean isSoldOut;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("children_discount")
    private BigDecimal childrenDiscount;

    @JsonProperty("students_discount")
    private BigDecimal studentsDiscount;

    @JsonProperty("start_at")
    private Date startAt;

    @JsonProperty("end_at")
    private Date endAt;

    @JsonProperty("is_without_seats")
    private boolean isWithoutSeats;
}
