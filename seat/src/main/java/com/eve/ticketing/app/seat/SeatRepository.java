package com.eve.ticketing.app.seat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long>, JpaSpecificationExecutor<Seat> {

    Optional<Seat> findFirstByEventIdAndOccupiedIsFalse(long eventId);

    long countByEventIdAndOccupiedTrue(long eventId);
}
