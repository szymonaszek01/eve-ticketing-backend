package com.eve.ticketing.app.seat;

import org.springframework.data.jpa.domain.Specification;

public class SeatSpecification {

    public static Specification<Seat> seatSectorEqual(String sector) {
        if (sector == null || sector.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("sector"), sector);
    }

    public static Specification<Seat> seatRowEqual(Integer row) {
        if (row == null || row < 1) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("row"), row);
    }

    public static Specification<Seat> seatNumberEqual(Integer number) {
        if (number == null || number < 1) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("number"), number);
    }
}
