package com.eve.ticketing.app.event;

import com.eve.ticketing.app.event.dto.CurrentTicketAmountDto;
import com.eve.ticketing.app.event.dto.EventFilterDto;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface EventService {

    void createEvent(Event event) throws EventProcessingException;

    void updateEventCurrentTicketAmount(CurrentTicketAmountDto currentTicketAmountDto) throws EventProcessingException;

    Event getEventById(long id) throws EventProcessingException;

    BigDecimal getEventCostById(long id, boolean isAdult, boolean isStudent) throws EventProcessingException;

    Page<Event> getEventList(int page, int size, EventFilterDto eventFilterDto);

    void deleteEventById(long id) throws EventProcessingException;
}
