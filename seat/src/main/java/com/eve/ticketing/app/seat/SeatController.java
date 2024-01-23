package com.eve.ticketing.app.seat;

import com.eve.ticketing.app.seat.dto.SeatFilterDto;
import com.eve.ticketing.app.seat.dto.SeatReserveDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Seat", description = "Seat management APIs")
@RequestMapping("/api/v1/seat")
@RequiredArgsConstructor
@RestController
public class SeatController {

    private final SeatServiceImpl seatService;

    @PostMapping("/create")
    public ResponseEntity<?> createSeat(@RequestBody Seat seat) {
        try {
            seatService.createOrUpdateSeat(seat);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (SeatProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Seat> getSeatById(@PathVariable long id) {
        try {
            Seat seat = seatService.getSeatById(id);
            return new ResponseEntity<>(seat, HttpStatus.OK);
        } catch (SeatProcessingException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<Seat>> getSeatList(@RequestParam(value = "page") int page,
                                                  @RequestParam(value = "size") int size,
                                                  SeatFilterDto SeatFilterDto) {
        Page<Seat> seatPage = seatService.getSeatList(page, size, SeatFilterDto);
        return new ResponseEntity<>(seatPage, HttpStatus.OK);
    }

    @PutMapping("/reserve")
    public ResponseEntity<Long> reserveSeat(@RequestBody SeatReserveDto seatReserveDto) {
        try {
            Seat seat = seatService.reserveSeat(seatReserveDto);
            return new ResponseEntity<>(seat.getId(), HttpStatus.OK);
        } catch (SeatProcessingException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteSeatById(@PathVariable long id) {
        try {
            seatService.deleteSeatById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (SeatProcessingException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
