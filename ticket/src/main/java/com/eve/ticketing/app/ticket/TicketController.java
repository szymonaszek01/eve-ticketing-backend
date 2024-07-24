package com.eve.ticketing.app.ticket;

import com.eve.ticketing.app.ticket.dto.TicketDto;
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
    public ResponseEntity<?> getTicketList(@RequestParam(value = "page") int page,
                                           @RequestParam(value = "size") int size,
                                           @RequestParam(defaultValue = "id,desc") String[] sort,
                                           @RequestHeader("Authorization") String token,
                                           TicketFilterDto ticketFilterDto) {
        Page<Ticket> ticketPage = ticketService.getTicketList(page, size, ticketFilterDto, sort, token);
        return new ResponseEntity<>(ticketPage, HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable long id) {
        Ticket ticket = ticketService.getTicketById(id);
        return new ResponseEntity<>(ticket, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTicket(@Valid @RequestBody TicketDto ticketDto, @RequestHeader("Authorization") String token) {
        Ticket ticket = ticketService.createTicket(ticketDto, token);
        return new ResponseEntity<>(ticket, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateTicket(@RequestBody HashMap<String, Object> values) {
        Ticket ticket = ticketService.updateTicket(values);
        return new ResponseEntity<>(ticket, HttpStatus.OK);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteTicketById(@PathVariable long id) {
        ticketService.deleteTicketById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/pay")
    public ResponseEntity<?> payForTicketList(@RequestBody HashMap<String, Object> values, @RequestHeader("Authorization") String token) {
        ticketService.payForTicketList(values, token);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
