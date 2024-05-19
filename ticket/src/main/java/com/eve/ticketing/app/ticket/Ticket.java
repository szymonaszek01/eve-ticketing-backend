package com.eve.ticketing.app.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "Field \"code\" should not be blank")
    private String code;

    @Column(nullable = false)
    @JsonProperty("created_at")
    @NotNull(message = "Field \"created_at\" should not be \"null\"")
    @FutureOrPresent(message = "Field \"created_at\" should be present or future date")
    private Date createdAt;

    @Column(nullable = false)
    @NotBlank(message = "Field \"firstname\" should not be blank")
    private String firstname;

    @Column(nullable = false)
    @NotBlank(message = "Field \"lastname\" should not be blank")
    private String lastname;

    @Column(nullable = false)
    @JsonProperty("phone_number")
    @NotBlank(message = "Field \"phone_number\" should not be blank")
    private String phoneNumber;

    @Column(nullable = false)
    @NotNull(message = "Field \"cost\" should not be \"null\"")
    @Min(value = 1, message = "Field \"cost\" should be greater than \"0\"")
    private BigDecimal cost;

    @JsonProperty("is_adult")
    @NotNull(message = "Field \"is_adult\" should not be \"null\"")
    private Boolean isAdult;

    @JsonProperty("is_student")
    @NotNull(message = "Field \"is_student\" should not be \"null\"")
    private Boolean isStudent;

    @JsonProperty("event_id")
    @NotNull(message = "Field \"event_id\" should not be \"null\"")
    @Min(value = 1, message = "Field \"event_id\" should be greater than \"0\"")
    private Long eventId;

    @JsonProperty("seat_id")
    @NotNull(message = "Field \"seat_id\" should not be \"null\"")
    @Min(value = 1, message = "Field \"seat_id\" should be greater than \"0\"")
    private Long seatId;
}
