package com.eve.ticketing.app.smsnotification.kafka;

import com.eve.ticketing.app.smsnotification.dto.NotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class KafkaNotificationDeserializer implements Deserializer<NotificationDto> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public NotificationDto deserialize(String s, byte[] bytes) {
        try {
            if (bytes == null) {
                log.error("Null received at deserializing");
                return null;
            }
            return objectMapper.readValue(new String(bytes, StandardCharsets.UTF_8), NotificationDto.class);
        } catch (Exception e) {
            throw new SerializationException("Error when deserializing byte[] to MessageDto");
        }
    }

    @Override
    public void close() {
        Deserializer.super.close();
    }
}
