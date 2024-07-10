package com.eve.ticketing.app.ticket;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Date;

public class TicketSpecification {

    public static Specification<Ticket> ticketCodeEqual(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.upper(root.get("code")), "%" + code.toUpperCase() + "%");
    }

    public static Specification<Ticket> ticketFirstnameEqual(String firstname) {
        if (StringUtils.isBlank(firstname)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.upper(root.get("firstname")), "%" + firstname.toUpperCase() + "%");
    }

    public static Specification<Ticket> ticketLastnameEqual(String lastname) {
        if (StringUtils.isBlank(lastname)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.upper(root.get("lastname")), "%" + lastname.toUpperCase() + "%");
    }

    public static Specification<Ticket> ticketPhoneNumberEqual(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.upper(root.get("phoneNumber")), "%" + phoneNumber.toUpperCase() + "%");
    }

    public static Specification<Ticket> ticketCostBetween(Double minCost, Double maxCost) {
        if (minCost == null && maxCost == null) {
            return null;
        }
        double finalMinCost = (minCost == null) ? 0 : minCost;
        double finalMaxCost = (maxCost == null) ? 0 : maxCost;
        if (finalMinCost < 0 || finalMaxCost < 0 || finalMinCost > finalMaxCost) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("cost"), BigDecimal.valueOf(finalMinCost), BigDecimal.valueOf(finalMaxCost));
    }

    public static Specification<Ticket> ticketCreatedAtBetween(Date minDate, Date maxDate) {
        if (minDate == null && maxDate == null) {
            return null;
        }
        Date finalMinDate = (minDate == null) ? new Date(System.currentTimeMillis()) : minDate;
        Date finalMaxDate = (maxDate == null) ? new Date(System.currentTimeMillis()) : maxDate;
        if (finalMinDate.after(finalMaxDate)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("createdAt"), finalMinDate, finalMaxDate);
    }

    public static Specification<Ticket> ticketUserIdEqual(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
    }

    public static Specification<Ticket> ticketEventIdEqual(Long eventId) {
        if (eventId == null || eventId <= 0) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("eventId"), eventId);
    }

    public static Specification<Ticket> ticketSeatIdEqual(Long seatId) {
        if (seatId == null || seatId <= 0) {
            return null;
        }
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("seatId"), seatId));
    }

    public static Specification<Ticket> ticketPaidEqual(Boolean paid) {
        if (paid == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paid"), paid);
    }
}
