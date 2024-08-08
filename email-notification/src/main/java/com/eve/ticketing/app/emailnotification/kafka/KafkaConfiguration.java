package com.eve.ticketing.app.emailnotification.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("spring.kafka")
public class KafkaConfiguration {

    private String bootstrapServers;
}
