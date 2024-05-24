package com.eve.ticketing.app.authuser.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserFilterDto {

    private String email;

    private String firstname;

    private String lastname;

    private String phoneNumber;
}
