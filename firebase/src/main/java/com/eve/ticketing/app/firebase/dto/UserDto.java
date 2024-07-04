package com.eve.ticketing.app.firebase.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    @NotNull(message = "should not be null")
    private Long id;

    @Email(regexp = ".+[@].+[\\.].+", message = "invalid email address")
    @NotBlank(message = "should not be blank")
    private String email;
}
