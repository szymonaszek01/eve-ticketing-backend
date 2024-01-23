package com.eve.ticketing.app.ticket;

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
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    @JsonProperty("created_at")
    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @JsonProperty("identity_card_number")
    @Column(nullable = false)
    private String identityCardNumber;

    private BigDecimal cost;

    @JsonProperty("is_adult")
    private boolean isAdult;

    @JsonProperty("is_student")
    private boolean isStudent;

    @JsonProperty("event_id")
    private long eventId;

    @JsonProperty("seat_id")
    private long seatId;
}
