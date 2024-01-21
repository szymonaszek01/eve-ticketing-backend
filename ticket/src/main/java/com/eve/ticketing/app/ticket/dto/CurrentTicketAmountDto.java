package com.eve.ticketing.app.ticket.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrentTicketAmountDto {

    private long eventId;

    private long createdTickets;
}
