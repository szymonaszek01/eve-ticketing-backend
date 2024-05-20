package com.eve.ticketing.app.seat;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "should not be null")
    @Min(value = 0, message = "should be greater than or equal 0")
    private Integer row;

    @NotNull(message = "should not be null")
    @Min(value = 0, message = "should be greater than or equal 0")
    private Integer number;

    @NotNull(message = "should not be null")
    private Boolean occupied;

    @JsonProperty("event_id")
    @NotNull(message = "should not be null")
    @Min(value = 1, message = "should be greater than 0")
    private Long eventId;
}
