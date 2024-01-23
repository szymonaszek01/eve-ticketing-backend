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
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @JsonProperty("max_ticket_amount")
    private long maxTicketAmount;

    @JsonProperty("is_sold_out")
    private boolean isSoldOut;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    private String currency;

    @JsonProperty("children_discount")
    private BigDecimal childrenDiscount;

    @JsonProperty("students_discount")
    private BigDecimal studentsDiscount;

    @JsonProperty("start_at")
    @Column(nullable = false)
    private Date startAt;

    @JsonProperty("end_at")
    @Column(nullable = false)
    private Date endAt;

    private String country;

    private String address;

    @JsonProperty("localization_name")
    private String localizationName;

    @JsonProperty("is_without_seats")
    private boolean isWithoutSeats;
}
