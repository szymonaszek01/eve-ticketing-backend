package com.eve.ticketing.app.event;

import com.eve.ticketing.app.event.dto.EventFilterDto;
import com.eve.ticketing.app.event.dto.EventShortDescriptionDto;
import com.eve.ticketing.app.event.dto.EventSoldOutDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import static com.eve.ticketing.app.event.EventSpecification.*;

@Slf4j
@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public void createOrUpdateEvent(Event event) throws EventProcessingException {
        try {
            eventRepository.save(event);
            log.info("Event (name=\"{}\") was created/updated", event.getName());
        } catch (RuntimeException e) {
            log.error("Event (name=\"{}\") was not created/updated", event.getName());
            throw new EventProcessingException("Event was not created/updated - " + e.getMessage());
        }
    }

    @Override
    public void updateEventSoldOut(EventSoldOutDto eventSoldOutDto) throws EventProcessingException {
        Event event = getEventById(eventSoldOutDto.getEventId());
        event.setSoldOut(eventSoldOutDto.isSoldOut());
        createOrUpdateEvent(event);
    }

    @Override
    public Event getEventById(long id) throws EventProcessingException {
        return eventRepository.findById(id).orElseThrow(() -> {
            log.error("Event (id=\"{}\") was not found", id);
            throw new EventProcessingException("Event was not found - invalid event id");
        });
    }

    @Override
    public EventShortDescriptionDto getEventShortDescriptionById(long id) throws EventProcessingException {
        Event event = getEventById(id);
        return EventShortDescriptionDto.builder()
                .eventId(id)
                .name(event.getName())
                .maxTicketAmount(event.getMaxTicketAmount())
                .isSoldOut(event.isSoldOut())
                .unitPrice(event.getUnitPrice())
                .childrenDiscount(event.getChildrenDiscount())
                .studentsDiscount(event.getStudentsDiscount())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .isWithoutSeats(event.isWithoutSeats())
                .build();
    }

    @Override
    public Page<Event> getEventList(int page, int size, EventFilterDto eventFilterDto) {
        Specification<Event> eventSpecification = Specification.where(eventNameEqual(eventFilterDto.getName()))
                .and(eventUnitPriceBetween(eventFilterDto.getMinUnitPrice(), eventFilterDto.getMaxUnitPrice()))
                .and(eventStartAtBetween(eventFilterDto.getMinDate(), eventFilterDto.getMaxDate()))
                .and(eventEndAtBetween(eventFilterDto.getMinDate(), eventFilterDto.getMaxDate()))
                .and(eventCountryEqual(eventFilterDto.getCountry())).and(eventAddressEqual(eventFilterDto.getAddress()));
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
