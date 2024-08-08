package com.eve.ticketing.app.emailnotification.kafka;

import com.eve.ticketing.app.emailnotification.EmailSender;
import com.eve.ticketing.app.emailnotification.dto.EmailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Component
public class KafkaEmailListener {

    private final EmailSender emailSender;

    private final SpringTemplateEngine springTemplateEngine;

    @Autowired
    public KafkaEmailListener(EmailSender emailSender, SpringTemplateEngine springTemplateEngine) {
        this.emailSender = emailSender;
        this.springTemplateEngine = springTemplateEngine;
    }

    @KafkaListener(topics = "email", groupId = "groupId")
    void subscribe(EmailDto emailDto) {
        try {
            Context context = new Context();
            context.setVariable("data", emailDto.getData());
            String htmlToString = springTemplateEngine.process(emailDto.getTemplate(), context);
            emailDto.setEmail(htmlToString);
            emailSender.send(emailDto);
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
        }
    }
}
