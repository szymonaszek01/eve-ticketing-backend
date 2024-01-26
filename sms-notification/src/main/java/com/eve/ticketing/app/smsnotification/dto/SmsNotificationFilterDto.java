package com.eve.ticketing.app.smsnotification.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SmsNotificationFilterDto {

    private String phoneNumber;

    private String message;

    private Long ticketId;
}
