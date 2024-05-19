package com.eve.ticketing.app.seat;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
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

    @Column(nullable = false)
    @NotNull(message = "Field \"row\" should not be \"null\"")
    @Min(value = 1, message = "Field \"row\" should be greater than \"0\"")
    private Integer row;

    @Column(nullable = false)
    @NotNull(message = "Field \"number\" should not be \"null\"")
    @Min(value = 1, message = "Field \"number\" should be greater than \"0\"")
    private Integer number;

    @Column(nullable = false)
    @NotNull(message = "Field \"occupied\" should not be \"null\"")
    private Boolean occupied;

    @Column(nullable = false)
    @JsonProperty("event_id")
    @NotNull(message = "Field \"event_id\" should not be \"null\"")
    @Min(value = 1, message = "Field \"event_id\" should be greater than \"0\"")
    private Long eventId;
}
