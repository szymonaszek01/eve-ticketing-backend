package com.eve.ticketing.app.event;

import com.eve.ticketing.app.event.dto.EventFilterDto;
import com.eve.ticketing.app.event.dto.EventShortDescriptionDto;
import com.eve.ticketing.app.event.dto.EventSoldOutDto;
import org.springframework.data.domain.Page;

public interface EventService {

    void createOrUpdateEvent(Event event) throws EventProcessingException;

    void updateEventSoldOut(EventSoldOutDto eventSoldOutDto) throws EventProcessingException;

    Event getEventById(long id) throws EventProcessingException;

    EventShortDescriptionDto getEventShortDescriptionById(long id) throws EventProcessingException;

    Page<Event> getEventList(int page, int size, EventFilterDto eventFilterDto);

    void deleteEventById(long id) throws EventProcessingException;
}
