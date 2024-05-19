package com.eve.ticketing.app.ticket;

import com.eve.ticketing.app.ticket.dto.TicketFilterDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;

@Tag(name = "Ticket", description = "Ticket management APIs")
@RequestMapping("/api/v1/ticket")
@RequiredArgsConstructor
@RestController
public class TicketController {

    private final TicketServiceImpl ticketService;

    @GetMapping("/all")
    public ResponseEntity<Page<Ticket>> getTicketList(@RequestParam(value = "page") int page,
                                                      @RequestParam(value = "size") int size,
                                                      TicketFilterDto ticketFilterDto) {
        Page<Ticket> ticketPage = ticketService.getTicketList(page, size, ticketFilterDto);
        return new ResponseEntity<>(ticketPage, HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable long id) {
        try {
            Ticket ticket = ticketService.getTicketById(id);
            return new ResponseEntity<>(ticket, HttpStatus.OK);
        } catch (TicketProcessingException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTicket(@Valid @RequestBody Ticket ticket) {
        try {
            ticketService.createTicket(ticket);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (TicketProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Ticket> updateEvent(@RequestBody HashMap<String, Object> values) {
        try {
            Ticket ticket = ticketService.updateTicket(values);
            return new ResponseEntity<>(ticket, HttpStatus.OK);
        } catch (TicketProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteTicketById(@PathVariable long id) {
        try {
            ticketService.deleteTicketById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (TicketProcessingException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
