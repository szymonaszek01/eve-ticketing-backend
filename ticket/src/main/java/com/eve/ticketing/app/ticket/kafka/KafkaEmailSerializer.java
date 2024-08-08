package com.eve.ticketing.app.ticket.kafka;

import com.eve.ticketing.app.ticket.dto.EmailDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class KafkaEmailSerializer implements Serializer<EmailDto> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String s, EmailDto emailDto) {
        try {
            if (emailDto == null) {
                log.error("Null received at serializing");
                return null;
            }
            return objectMapper.writeValueAsBytes(emailDto);
        } catch (Exception e) {
            throw new SerializationException("Error when serializing emailDto to byte[]");
        }
    }

    @Override
    public void close() {
        Serializer.super.close();
    }
}
