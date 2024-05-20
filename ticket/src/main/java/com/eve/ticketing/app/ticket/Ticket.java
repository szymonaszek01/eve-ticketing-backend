package com.eve.ticketing.app.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @NotBlank(message = "should not be blank")
    private String code;

    @JsonProperty("created_at")
    @NotNull(message = "should not be null")
    @FutureOrPresent(message = "should be present or future date")
    private Date createdAt;

    @NotBlank(message = "should not be blank")
    private String firstname;

    @NotBlank(message = "should not be blank")
    private String lastname;

    @JsonProperty("phone_number")
    @NotBlank(message = "should not be blank")
    private String phoneNumber;

    @NotNull(message = "should not be null")
    @Min(value = 0, message = "should be greater than or equal 0")
    private BigDecimal cost;

    @JsonProperty("is_adult")
    @NotNull(message = "should not be null")
    private Boolean isAdult;

    @JsonProperty("is_student")
    @NotNull(message = "should not be null")
    private Boolean isStudent;

    @JsonProperty("event_id")
    @NotNull(message = "should not be null")
    @Min(value = 1, message = "should be greater than 0")
    private Long eventId;

    @JsonProperty("seat_id")
    @NotNull(message = "should not be null")
    @Min(value = 1, message = "should be greater than 0")
    private Long seatId;
}
