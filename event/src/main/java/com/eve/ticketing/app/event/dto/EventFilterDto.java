package com.eve.ticketing.app.event.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventFilterDto {

    private String name;

    private Double minUnitPrice;

    private Double maxUnitPrice;

    private String minDate;

    private String maxDate;

    private String country;

    private String address;
}
