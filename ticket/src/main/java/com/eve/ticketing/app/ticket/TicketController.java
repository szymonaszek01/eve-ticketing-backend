package com.eve.ticketing.app.ticket;

import com.eve.ticketing.app.ticket.dto.TicketFilterDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        Ticket ticket = ticketService.getTicketById(id);
        return new ResponseEntity<>(ticket, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket) {
        ticketService.createTicket(ticket);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<Ticket> updateEvent(@RequestBody HashMap<String, Object> values) {
        Ticket ticket = ticketService.updateTicket(values);
        return new ResponseEntity<>(ticket, HttpStatus.OK);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteTicketById(@PathVariable long id) {
        ticketService.deleteTicketById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
