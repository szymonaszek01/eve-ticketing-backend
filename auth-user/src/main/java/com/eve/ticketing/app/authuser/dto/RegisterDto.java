package com.eve.ticketing.app.authuser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {

    @Email(regexp = ".+[@].+[\\.].+", message = "invalid email address")
    @NotBlank(message = "should not be blank")
    private String email;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "should be at least 8 characters long, contain at least one digit, one uppercase letter, one lowercase letter and one special character, and must not contain any whitespace.")
    @NotBlank(message = "should not be blank")
    private String password;

    @NotBlank(message = "should not be blank")
    private String firstname;

    @NotBlank(message = "should not be blank")
    private String lastname;

    @JsonProperty("phone_number")
    @NotBlank(message = "should not be blank")
    private String phoneNumber;
}
