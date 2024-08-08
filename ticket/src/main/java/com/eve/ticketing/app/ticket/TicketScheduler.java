package com.eve.ticketing.app.ticket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class TicketScheduler {

    private static final int PAYMENT_DURATION = 10;

    private final TicketServiceImpl ticketService;

    @Scheduled(fixedRate = 60 * 1000)
    public void removeUnpaidTickets() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -PAYMENT_DURATION);
        Date createdAt = calendar.getTime();
        List<Ticket> ticketList = ticketService.getTicketListByPaidIsFalseAndCreatedAt(createdAt);
        ticketList.forEach(ticket -> ticketService.deleteTicketById(ticket.getId()));
        log.info("Result of ticket scheduler - removed unpaid tickets ({})", ticketList.size());
    }
}
