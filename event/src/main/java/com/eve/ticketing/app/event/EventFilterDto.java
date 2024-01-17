package com.eve.ticketing.app.event;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class EventFilterDto {

    private String name;

    private Double minUnitPrice;

    private Double maxUnitPrice;

    private Date minDate;

    private Date maxDate;

    private String country;

    private String address;
}
