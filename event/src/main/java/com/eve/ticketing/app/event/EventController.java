package com.eve.ticketing.app.event;

import com.eve.ticketing.app.event.dto.EventFilterDto;
import com.eve.ticketing.app.event.dto.EventShortDescriptionDto;
import com.eve.ticketing.app.event.dto.EventSoldOutDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Event", description = "Event management APIs")
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
@RestController
public class EventController {

    private final EventServiceImpl eventService;

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        try {
            eventService.createOrUpdateEvent(event);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (EventProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/update/sold-out")
    public ResponseEntity<?> updateEventSoldOut(@Valid @RequestBody EventSoldOutDto eventSoldOutDto) {
        try {
            eventService.updateEventSoldOut(eventSoldOutDto);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EventProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable long id) {
        try {
            Event event = eventService.getEventById(id);
            return new ResponseEntity<>(event, HttpStatus.OK);
        } catch (EventProcessingException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/id/{id}/short-description")
    public ResponseEntity<EventShortDescriptionDto> getEventShortDescriptionById(@PathVariable long id) {
        try {
            EventShortDescriptionDto eventShortDescriptionDto = eventService.getEventShortDescriptionById(id);
            return new ResponseEntity<>(eventShortDescriptionDto, HttpStatus.OK);
        } catch (EventProcessingException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<Event>> getEventList(@RequestParam(value = "page") int page,
                                                    @RequestParam(value = "size") int size,
                                                    EventFilterDto eventFilterDto) {
        Page<Event> eventPage = eventService.getEventList(page, size, eventFilterDto);
        return new ResponseEntity<>(eventPage, HttpStatus.OK);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteEventById(@PathVariable long id) {
        try {
            eventService.deleteEventById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EventProcessingException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
