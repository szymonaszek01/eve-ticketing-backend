package com.eve.ticketing.app.event;

import org.springframework.data.domain.Page;

public interface EventService {

    void createEvent(Event event) throws EventProcessingException;

    Event getEventById(long id) throws EventProcessingException;

    Page<Event> getEventList(int page, int size, EventFilterDto eventFilterDto);

    void deleteEventById(long id) throws EventProcessingException;
}
