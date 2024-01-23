package com.eve.ticketing.app.seat;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sector;

    @Column(nullable = false)
    private int row;

    @Column(nullable = false)
    private int number;

    @JsonProperty("occupied")
    private boolean occupied;

    @JsonProperty("event_id")
    private long eventId;
}
