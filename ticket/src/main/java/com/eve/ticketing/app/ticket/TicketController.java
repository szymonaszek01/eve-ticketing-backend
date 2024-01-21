package com.eve.ticketing.app.ticket;

import com.eve.ticketing.app.ticket.dto.TicketFilterDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Ticket", description = "Ticket management APIs")
@RequestMapping("/api/v1/ticket")
@RequiredArgsConstructor
@RestController
public class TicketController {

    private final TicketServiceImpl ticketService;

    @PostMapping("/create")
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket) {
        try {
            ticketService.createTicket(ticket);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (TicketProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
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

    @GetMapping("/all")
    public ResponseEntity<Page<Ticket>> getTicketList(@RequestParam(value = "page") int page,
                                                      @RequestParam(value = "size") int size,
                                                      TicketFilterDto ticketFilterDto) {
        Page<Ticket> ticketPage = ticketService.getTicketList(page, size, ticketFilterDto);
        return new ResponseEntity<>(ticketPage, HttpStatus.OK);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteTicketById(@PathVariable long id) {
        try {
            // TODO: Decrease value in "currentTicketAmount" field in Event
            ticketService.deleteTicketById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (TicketProcessingException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
