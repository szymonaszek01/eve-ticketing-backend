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

    @Column(nullable = false)
    @JsonProperty("phone_number")
    @NotBlank(message = "Field \"phone_number\" should not be blank")
    private String phoneNumber;

    @Column(nullable = false)
    @NotBlank(message = "Field \"message\" should not be blank")
    private String message;

    @Column(nullable = false)
    @JsonProperty("ticket_id")
    @NotNull(message = "Field \"ticket_id\" should not be \"null\"")
    @Min(value = 1, message = "Field \"ticket_id\" should be greater than \"0\"")
    private Long ticketId;
}
