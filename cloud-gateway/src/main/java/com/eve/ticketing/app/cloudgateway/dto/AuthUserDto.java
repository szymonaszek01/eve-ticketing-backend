package com.eve.ticketing.app.cloudgateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserDto {

    private Long id;

    private String email;

    @JsonProperty("created_at")
    private Date createdAt;

    private String role;
}
