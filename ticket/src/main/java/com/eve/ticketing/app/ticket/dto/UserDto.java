package com.eve.ticketing.app.ticket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.*;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    @NotNull(message = "should not be null")
    @Min(value = 1, message = "should be greater than 0")
    private Long id;

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

    @Transient
    @JsonProperty("auth_token")
    private String authToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @Pattern(regexp = "USER|ADMIN", message = "should be either USER or ADMIN")
    @NotBlank(message = "should not be blank")
    private String role;
}
