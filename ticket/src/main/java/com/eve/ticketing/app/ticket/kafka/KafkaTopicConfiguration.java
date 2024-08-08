package com.eve.ticketing.app.ticket.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.List;
import java.util.stream.Stream;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public KafkaAdmin.NewTopics createTopics() {
        List<NewTopic> topicList = Stream.of("notification", "email")
                .map(topicName -> TopicBuilder.name(topicName).partitions(1).replicas(1).build())
                .toList();
        return new KafkaAdmin.NewTopics(topicList.toArray(new NewTopic[0]));
    }
}
