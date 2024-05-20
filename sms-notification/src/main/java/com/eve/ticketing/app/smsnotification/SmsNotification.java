package com.eve.ticketing.app.smsnotification;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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

    @JsonProperty("phone_number")
    @NotBlank(message = "should not be blank")
    private String phoneNumber;

    @NotBlank(message = "should not be blank")
    private String message;

    @JsonProperty("ticket_id")
    @NotNull(message = "should not be null")
    @Min(value = 1, message = "should be greater than 0")
    private Long ticketId;
}
