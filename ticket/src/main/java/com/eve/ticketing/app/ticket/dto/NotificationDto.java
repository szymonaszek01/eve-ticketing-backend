package com.eve.ticketing.app.ticket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {

    @Email(regexp = ".+[@].+[\\.].+", message = "invalid email address")
    @NotBlank(message = "should not be blank")
    private String email;

    @NotBlank(message = "should not be blank")
    private String firstname;

    @NotBlank(message = "should not be blank")
    private String lastname;

    @JsonProperty("phone_number")
    @NotBlank(message = "should not be blank")
    private String phoneNumber;

    @NotBlank(message = "should not be blank")
    private String message;

    @NotNull(message = "should not be null")
    private Long ticketId;
}
