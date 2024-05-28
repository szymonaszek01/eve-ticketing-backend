package com.eve.ticketing.app.smsnotification.kafka;

import com.eve.ticketing.app.smsnotification.SmsNotification;
import com.eve.ticketing.app.smsnotification.SmsNotificationServiceImpl;
import com.eve.ticketing.app.smsnotification.SmsSender;
import com.eve.ticketing.app.smsnotification.dto.NotificationDto;
import com.eve.ticketing.app.smsnotification.exception.SmsNotificationProcessingException;
import com.eve.ticketing.app.smsnotification.twilio.TwilioSmsSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class KafkaNotificationListener {

    private final SmsSender smsSender;

    private final SmsNotificationServiceImpl smsNotificationService;

    @Autowired
    public KafkaNotificationListener(@Qualifier("twilio") TwilioSmsSender smsSender, SmsNotificationServiceImpl smsNotificationService) {
        this.smsSender = smsSender;
        this.smsNotificationService = smsNotificationService;
    }

    @KafkaListener(topics = "notification", groupId = "groupId")
    void subscribe(NotificationDto notificationDto) {
        try {
            SmsNotification smsNotification = SmsNotification.builder()
                    .phoneNumber(notificationDto.getPhoneNumber())
                    .message(notificationDto.getMessage())
                    .ticketId(notificationDto.getTicketId())
                    .build();
            smsSender.sendSms(smsNotification);
            smsNotificationService.createSmsNotification(smsNotification);
        } catch (SmsNotificationProcessingException e) {
            log.error(e.getError().toString());
        }
    }
}
