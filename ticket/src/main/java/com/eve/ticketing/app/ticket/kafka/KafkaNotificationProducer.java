package com.eve.ticketing.app.ticket.kafka;

import com.eve.ticketing.app.ticket.dto.NotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaNotificationProducer {

    private final KafkaTemplate<String, NotificationDto> kafkaTemplate;

    @Autowired
    public KafkaNotificationProducer(KafkaTemplate<String, NotificationDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(NotificationDto notificationDto) {
        try {
            kafkaTemplate.send("notification", notificationDto);
            log.error("Notification for ticket (ticketId={}) was published successfully", notificationDto.getTicketId());
        } catch (RuntimeException e) {
            log.error("Notification for ticket (ticketId={}) was not published - {}", notificationDto.getTicketId(), e.getMessage());
        }

    }
}
