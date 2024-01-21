package com.eve.ticketing.app.event;

import com.eve.ticketing.app.event.dto.CurrentTicketAmountDto;
import com.eve.ticketing.app.event.dto.EventFilterDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Tag(name = "Event", description = "Event management APIs")
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
@RestController
public class EventController {

    private final EventServiceImpl eventService;

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        try {
            eventService.createEvent(event);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (EventProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/update/current-ticket-amount")
    public ResponseEntity<?> updateEventCurrentTicketAmount(@RequestBody CurrentTicketAmountDto currentTicketAmountDto) {
        try {
            eventService.updateEventCurrentTicketAmount(currentTicketAmountDto);
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

    @GetMapping("/id/{id}/cost")
    public ResponseEntity<BigDecimal> getEventCostById(@PathVariable long id,
                                                       @RequestParam boolean isAdult,
                                                       @RequestParam boolean isStudent) {
        try {
            BigDecimal eventCost = eventService.getEventCostById(id, isAdult, isStudent);
            return new ResponseEntity<>(eventCost, HttpStatus.OK);
        } catch (EventProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
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
