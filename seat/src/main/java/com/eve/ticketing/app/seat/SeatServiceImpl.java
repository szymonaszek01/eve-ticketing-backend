package com.eve.ticketing.app.seat;

import com.eve.ticketing.app.seat.dto.EventSoldOutDto;
import com.eve.ticketing.app.seat.dto.SeatCancelDto;
import com.eve.ticketing.app.seat.dto.SeatFilterDto;
import com.eve.ticketing.app.seat.dto.SeatReserveDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static com.eve.ticketing.app.seat.SeatSpecification.*;

@Slf4j
@Service
@AllArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    private final RestTemplate restTemplate;

    @Override
    public void createOrUpdateSeat(Seat seat) throws SeatProcessingException {
        try {
            seatRepository.save(seat);
            log.info("Seat (seatId=\"{}\", eventId=\"{}\") was created/updated", seat.getId(), seat.getEventId());
        } catch (RuntimeException e) {
            log.error("Seat (seatId=\"{}\", eventId=\"{}\") was not created/updated", seat.getId(), seat.getEventId());
            throw new SeatProcessingException("Seat was not created/updated - " + e.getMessage());
        }
    }

    @Override
    public Seat getSeatById(long id) throws SeatProcessingException {
        return seatRepository.findById(id).orElseThrow(() -> {
            log.error("Seat (id=\"{}\") was not found", id);
            throw new SeatProcessingException("Seat was not found - invalid seat id");
        });
    }

    @Override
    public Seat reserveSeat(SeatReserveDto seatReserveDto) throws SeatProcessingException {
        Seat seat = seatRepository.findFirstByEventIdAndOccupiedIsFalse(seatReserveDto.getEventId()).orElseThrow(() -> {
            log.error("Seat (eventId=\"{}\") was not found", seatReserveDto.getEventId());
            throw new SeatProcessingException("Seat was not found - invalid event id");
        });

        seat.setOccupied(true);
        long currentTicketAmount = seatRepository.countByEventIdAndOccupiedTrue(seatReserveDto.getEventId());
        if (currentTicketAmount + 1 == seatReserveDto.getMaxTicketAmount()) {
            setEventSoldOut(EventSoldOutDto.builder()
                    .eventId(seatReserveDto.getEventId())
                    .isSoldOut(true)
                    .build());
        }
        createOrUpdateSeat(seat);

        return seat;
    }

    @Override
    public void cancelSeat(SeatCancelDto seatCancelDto) throws SeatProcessingException {
        Seat seat = getSeatById(seatCancelDto.getSeatId());
        seat.setOccupied(false);
        if (seatCancelDto.getIsSoldOut()) {
            setEventSoldOut(EventSoldOutDto.builder()
                    .eventId(seat.getEventId())
                    .isSoldOut(false)
                    .build());
        }
        createOrUpdateSeat(seat);
    }

    @Override
    public Page<Seat> getSeatList(int page, int size, SeatFilterDto seatFilterDto) {
        Specification<Seat> seatSpecification = Specification.where(seatSectorEqual(seatFilterDto.getSector()))
                .and(seatRowEqual(seatFilterDto.getRow()))
                .and(seatNumberEqual(seatFilterDto.getNumber()));
        Pageable pageable = PageRequest.of(page, size);

        return seatRepository.findAll(seatSpecification, pageable);
    }

    @Override
    public void deleteSeatById(long id) throws SeatProcessingException {
        try {
            seatRepository.deleteById(id);
            log.info("Seat (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            log.error("Seat (id=\"{}\") was not deleted", id);
            throw new SeatProcessingException("Seat was not deleted - invalid seat id");
        }
    }

    private void setEventSoldOut(EventSoldOutDto eventSoldOutDto) {
        try {
            restTemplate.exchange(
                    "http://EVENT/api/v1/event/update/sold-out",
                    HttpMethod.PUT,
                    new HttpEntity<>(EventSoldOutDto.builder()
                            .eventId(eventSoldOutDto.getEventId())
                            .isSoldOut(true)
                            .build()),
                    void.class
            );
        } catch (RestClientException e) {
            log.error("Event (eventId={}) was not sold out - {}", eventSoldOutDto.getEventId(), e.getMessage());
            throw new SeatProcessingException("Unable to communicate with event server");
        }
    }
}
