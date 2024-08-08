package com.eve.ticketing.app.ticket.kafka;

import com.eve.ticketing.app.ticket.dto.EmailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaEmailProducer {

    private final KafkaTemplate<String, EmailDto> kafkaTemplate;

    @Autowired
    public KafkaEmailProducer(KafkaTemplate<String, EmailDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(EmailDto emailDto) {
        try {
            kafkaTemplate.send("email", emailDto);
            log.info("Email for ticket (ticketId={}) was published successfully", emailDto.getData().getOrDefault("id", "-"));
        } catch (RuntimeException e) {
            log.error("Email for ticket (ticketId={}) was not published - {}", emailDto.getData().getOrDefault("id", "-"), e.getMessage());
        }

    }
}
