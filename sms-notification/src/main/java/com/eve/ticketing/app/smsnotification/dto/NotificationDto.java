package com.eve.ticketing.app.smsnotification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {

    @NotNull
    @NotBlank
    private String email;

    @JsonProperty("phone_number")
    @NotNull
    @NotBlank
    private String phoneNumber;

    @JsonProperty("event_name")
    @NotNull
    @NotBlank
    private String firstname;

    @NotNull
    @NotBlank
    private String message;

    @NotNull
    private Long ticketId;
}
