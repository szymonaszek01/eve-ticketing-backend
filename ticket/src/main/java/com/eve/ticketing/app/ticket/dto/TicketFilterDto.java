package com.eve.ticketing.app.ticket.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketFilterDto {

    private String code;

    private String firstname;

    private String lastname;

    private String phoneNumber;

    private Double minCost;

    private Double maxCost;
}
