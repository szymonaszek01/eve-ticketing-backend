package com.eve.ticketing.app.event;

import com.eve.ticketing.app.event.dto.EventFilterDto;
import com.eve.ticketing.app.event.exception.EventProcessingException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.domain.Page;

import java.util.HashMap;

public interface EventService {

    Page<Event> getEventList(int page, int size, EventFilterDto eventFilterDto);

    Event getEventById(long id) throws EventProcessingException;

    void createEvent(Event event, String token) throws EventProcessingException, ConstraintViolationException;

    Event updateEvent(HashMap<String, Object> values) throws EventProcessingException, ConstraintViolationException;

    void deleteEventById(long id) throws EventProcessingException;
}
