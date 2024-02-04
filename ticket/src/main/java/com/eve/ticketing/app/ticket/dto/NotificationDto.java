package com.eve.ticketing.app.ticket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {

    private String email;

    @JsonProperty("phone_number")
    @NotNull
    @NotBlank
    private String phoneNumber;

    @JsonProperty("event_name")
    @NotNull
    @NotBlank
    private String firstname;

    @Column(nullable = false)
    @NotNull
    @NotBlank
    private String message;

    @NotNull
    private Long ticketId;
}
