package com.eve.ticketing.app.smsnotification;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SmsNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @JsonProperty("phone_number")
    @NotNull
    @NotBlank
    private String phoneNumber;

    @Column(nullable = false)
    @NotNull
    @NotBlank
    private String message;

    @Column(nullable = false)
    @JsonProperty("ticket_id")
    @NotNull
    private Long ticketId;
}
