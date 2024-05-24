package com.eve.ticketing.app.authuser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDto {

    @NotNull(message = "should not be null")
    private Long id;

    @JsonProperty("auth_token")
    @NotBlank(message = "should not be blank")
    private String authToken;

    @JsonProperty("refresh_token")
    @NotBlank(message = "should not be blank")
    private String refreshToken;
}
