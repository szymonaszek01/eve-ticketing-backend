package com.eve.ticketing.app.seat;

import com.eve.ticketing.app.seat.dto.SeatFilterDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;

@Tag(name = "Seat", description = "Seat management APIs")
@RequestMapping("/api/v1/seat")
@RequiredArgsConstructor
@RestController
public class SeatController {

    private final SeatServiceImpl seatService;

    @GetMapping("/all")
    public ResponseEntity<Page<Seat>> getSeatList(@RequestParam(value = "page") int page,
                                                  @RequestParam(value = "size") int size,
                                                  SeatFilterDto SeatFilterDto) {
        Page<Seat> seatPage = seatService.getSeatList(page, size, SeatFilterDto);
        return new ResponseEntity<>(seatPage, HttpStatus.OK);
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

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@Valid @RequestBody Seat seat) {
        try {
            seatService.createSeat(seat);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (SeatProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Seat> updateSeat(HashMap<String, Object> values) {
        try {
            Seat seat = seatService.updateSeat(values);
            return new ResponseEntity<>(seat, HttpStatus.OK);
        } catch (SeatProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
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
