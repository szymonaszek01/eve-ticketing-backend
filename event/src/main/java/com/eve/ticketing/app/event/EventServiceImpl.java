package com.eve.ticketing.app.event;

import com.eve.ticketing.app.event.dto.CurrentTicketAmountDto;
import com.eve.ticketing.app.event.dto.EventFilterDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

import static com.eve.ticketing.app.event.EventSpecification.*;

@Slf4j
@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public void createEvent(Event event) throws EventProcessingException {
        try {
            eventRepository.save(event);
            log.info("Event (name=\"{}\") was created", event.getName());
        } catch (RuntimeException e) {
            log.error("Event (name=\"{}\") was not created", event.getName());
            throw new EventProcessingException("Event was not created - invalid parameters");
        }
    }

    @Override
    public void updateEventCurrentTicketAmount(CurrentTicketAmountDto currentTicketAmountDto) throws EventProcessingException {
        if (currentTicketAmountDto.getCreatedTickets() < 1) {
            // TODO: Remove this case, when you will update "DELETE" method in TicketController
            log.error("Field \"currentTicketAmount\" in Event (id=\"{}\") was not updated - invalid parameters", currentTicketAmountDto.getEventId());
            throw new EventProcessingException("Invalid number of created tickets");
        }

        Event event = getEventById(currentTicketAmountDto.getEventId());
        if (new Date(System.currentTimeMillis()).after(event.getStartAt())) {
            log.error("Field \"currentTicketAmount\" in Event (id=\"{}\") was not updated - event has started", event.getId());
            throw new EventProcessingException("Event has started");
        }
        if (event.getCurrentTicketAmount() == event.getMaxTicketAmount()) {
            log.error("Field \"currentTicketAmount\" in Event (id=\"{}\") was not updated - sold out", event.getId());
            throw new EventProcessingException("Event has sold out all tickets");
        }

        event.setCurrentTicketAmount(event.getCurrentTicketAmount() + currentTicketAmountDto.getCreatedTickets());
        eventRepository.save(event);
        log.info("Field \"currentTicketAmount\" in Event (id=\"{}\") was updated to {} value", event.getId(), event.getCurrentTicketAmount());
    }

    @Override
    public Event getEventById(long id) throws EventProcessingException {
        return eventRepository.findById(id).orElseThrow(() -> {
            log.error("Event (id=\"{}\") was not found", id);
            throw new EventProcessingException("Event was not found. Invalid event id");
        });
    }

    @Override
    public BigDecimal getEventCostById(long id, boolean isAdult, boolean isStudent) throws EventProcessingException {
        Event event = getEventById(id);
        if (!isAdult && event.getChildrenDiscount() != null) {
            return event.getUnitPrice().multiply(BigDecimal.ONE.subtract(event.getChildrenDiscount()));
        }
        if (isAdult && isStudent && event.getStudentsDiscount() != null) {
            return event.getUnitPrice().multiply(BigDecimal.ONE.subtract(event.getStudentsDiscount()));
        }
        if (isAdult) {
            return event.getUnitPrice();
        }

        log.error("Unable to calculate Event (id=\"{}\") cost", id);
        throw new EventProcessingException("Event does not contain provided discounts - invalid parameters");
    }

    @Override
    public Page<Event> getEventList(int page, int size, EventFilterDto eventFilterDto) {
        Specification<Event> eventSpecification = Specification.where(eventNameEqual(eventFilterDto.getName())).and(eventUnitPriceBetween(eventFilterDto.getMinUnitPrice(), eventFilterDto.getMaxUnitPrice())).and(eventStartAtBetween(eventFilterDto.getMinDate(), eventFilterDto.getMaxDate())).and(eventEndAtBetween(eventFilterDto.getMinDate(), eventFilterDto.getMaxDate())).and(eventCountryEqual(eventFilterDto.getCountry())).and(eventAddressEqual(eventFilterDto.getAddress()));
        Pageable pageable = PageRequest.of(page, size);

        return eventRepository.findAll(eventSpecification, pageable);
    }

    @Override
    public void deleteEventById(long id) throws EventProcessingException {
        try {
            // TODO: Remove all tickets with provided id
            eventRepository.deleteById(id);
            log.info("Event (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            log.error("Event (id=\"{}\") was not deleted", id);
            throw new EventProcessingException("Event was not deleted - invalid event id");
        }
    }
}
