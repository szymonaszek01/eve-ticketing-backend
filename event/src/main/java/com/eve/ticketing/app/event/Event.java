package com.eve.ticketing.app.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    private String currency;

    @JsonProperty("start_at")
    @Column(nullable = false)
    private Date startAt;

    @JsonProperty("end_at")
    @Column(nullable = false)
    private Date endAt;

    private String country;

    private String city;

    private String street;

    @JsonProperty("postal_code")
    private String postalCode;
}
