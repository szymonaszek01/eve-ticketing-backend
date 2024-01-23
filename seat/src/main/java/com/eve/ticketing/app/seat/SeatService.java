package com.eve.ticketing.app.seat;

import com.eve.ticketing.app.seat.dto.SeatFilterDto;
import com.eve.ticketing.app.seat.dto.SeatReserveDto;
import org.springframework.data.domain.Page;

public interface SeatService {

    void createOrUpdateSeat(Seat seat) throws SeatProcessingException;

    Seat getSeatById(long id) throws SeatProcessingException;

    Seat reserveSeat(SeatReserveDto seatReserveDto) throws SeatProcessingException;

    Page<Seat> getSeatList(int page, int size, SeatFilterDto seatFilterDto);

    void deleteSeatById(long id) throws SeatProcessingException;
}
