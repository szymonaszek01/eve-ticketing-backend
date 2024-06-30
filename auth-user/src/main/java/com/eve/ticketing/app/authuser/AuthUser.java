package com.eve.ticketing.app.authuser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @Email(regexp = ".+[@].+[\\.].+", message = "invalid email address")
    @NotBlank(message = "should not be blank")
    private String email;

    @JsonIgnore
    private String password;

    @JsonProperty("created_at")
    @NotNull(message = "should not be null")
    private Date createdAt;

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

    @Pattern(regexp = "LOCAL|GOOGLE", message = "should be either LOCAL or GOOGLE")
    @NotBlank(message = "should not be blank")
    private String authProvider;
}
