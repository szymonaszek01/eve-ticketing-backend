package com.eve.ticketing.app.ticket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketDto {

    @NotBlank(message = "should not be blank")
    private String firstname;

    @NotBlank(message = "should not be blank")
    private String lastname;

    @JsonProperty("phone_number")
    @NotBlank(message = "should not be blank")
    private String phoneNumber;

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

    @JsonProperty("user_id")
    @NotNull(message = "should not be null")
    @Min(value = 1, message = "should be greater than 0")
    private Long userId;
}
