package com.eve.ticketing.app.event;

import com.eve.ticketing.app.event.dto.EventFilterDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Tag(name = "Event", description = "Event management APIs")
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
@RestController
public class EventController {

    private final EventServiceImpl eventService;

    @GetMapping("/all")
    public ResponseEntity<Page<Event>> getEventList(@RequestParam(value = "page") int page,
                                                    @RequestParam(value = "size") int size,
                                                    EventFilterDto eventFilterDto) {
        Page<Event> eventPage = eventService.getEventList(page, size, eventFilterDto);
        return new ResponseEntity<>(eventPage, HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable long id) {
        Event event = eventService.getEventById(id);
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@Valid @RequestBody Event event) {
        eventService.createEvent(event);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<Event> updateEvent(@RequestBody HashMap<String, Object> values) {
        Event event = eventService.updateEvent(values);
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteEventById(@PathVariable long id) {
        eventService.deleteEventById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
