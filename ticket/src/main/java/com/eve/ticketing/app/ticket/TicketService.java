package com.eve.ticketing.app.ticket;

import com.eve.ticketing.app.ticket.dto.TicketFilterDto;
import org.springframework.data.domain.Page;

public interface TicketService {

    void createOrUpdateTicket(Ticket ticket) throws TicketProcessingException;

    Ticket getTicketById(long id) throws TicketProcessingException;

    Page<Ticket> getTicketList(int page, int size, TicketFilterDto ticketFilterDto);

    void deleteTicketById(long id) throws TicketProcessingException;
}
