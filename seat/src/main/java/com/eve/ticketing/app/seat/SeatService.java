package com.eve.ticketing.app.seat;

import com.eve.ticketing.app.seat.dto.SeatFilterDto;
import com.eve.ticketing.app.seat.exception.SeatProcessingException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.domain.Page;

import java.util.HashMap;

public interface SeatService {

    Page<Seat> getSeatList(int page, int size, SeatFilterDto seatFilterDto);

    Seat getSeatById(long id) throws SeatProcessingException;

    void createSeat(Seat seat) throws SeatProcessingException, ConstraintViolationException;

    Seat updateSeat(HashMap<String, Object> values) throws SeatProcessingException, ConstraintViolationException;

    void deleteSeatById(long id) throws SeatProcessingException;
}
