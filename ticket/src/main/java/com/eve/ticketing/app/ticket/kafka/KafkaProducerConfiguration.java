package com.eve.ticketing.app.ticket.kafka;

import com.eve.ticketing.app.ticket.dto.EmailDto;
import com.eve.ticketing.app.ticket.dto.NotificationDto;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@AllArgsConstructor
public class KafkaProducerConfiguration {

    private final KafkaConfiguration kafkaConfiguration;

    @Bean
    public Map<String, Object> notificationProducerConfiguration() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfiguration.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaNotificationSerializer.class);
        return props;
    }

    @Bean
    public Map<String, Object> emailProducerConfiguration() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfiguration.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaEmailSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, NotificationDto> notifiactionProducerFactory() {
        return new DefaultKafkaProducerFactory<>(notificationProducerConfiguration());
    }

    @Bean
    public ProducerFactory<String, EmailDto> emailProducerFactory() {
        return new DefaultKafkaProducerFactory<>(emailProducerConfiguration());
    }

    @Bean
    public KafkaTemplate<String, NotificationDto> kafkaTemplateNotificationDto(ProducerFactory<String, NotificationDto> notifiactionProducerFactory) {
        return new KafkaTemplate<>(notifiactionProducerFactory);
    }

    @Bean
    public KafkaTemplate<String, EmailDto> kafkaTemplateEmailDto(ProducerFactory<String, EmailDto> emailProducerFactory) {
        return new KafkaTemplate<>(emailProducerFactory);
    }
}
