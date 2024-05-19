package com.eve.ticketing.app.seat;

import com.eve.ticketing.app.seat.dto.SeatFilterDto;
import org.springframework.data.domain.Page;

import java.util.HashMap;

public interface SeatService {

    Page<Seat> getSeatList(int page, int size, SeatFilterDto seatFilterDto);

    Seat getSeatById(long id) throws SeatProcessingException;

    void createSeat(Seat seat) throws SeatProcessingException;

    Seat updateSeat(HashMap<String, Object> values) throws SeatProcessingException;

    void deleteSeatById(long id) throws SeatProcessingException;
}
