package com.eve.ticketing.app.event.dto;

import lombok.*;

import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventFilterDto {

    private String name;

    private Double minUnitPrice;

    private Double maxUnitPrice;

    private Date minDate;

    private Date maxDate;

    private String country;

    private String address;
}
