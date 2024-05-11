package com.eve.ticketing.app.event;

import com.eve.ticketing.app.event.dto.EventFilterDto;
import org.springframework.data.domain.Page;

import java.util.HashMap;

public interface EventService {

    Page<Event> getEventList(int page, int size, EventFilterDto eventFilterDto);

    Event getEventById(long id) throws EventProcessingException;

    void createEvent(Event event) throws EventProcessingException;

    void updateEvent(HashMap<String, Object> values) throws EventProcessingException;

    void deleteEventById(long id) throws EventProcessingException;
}
