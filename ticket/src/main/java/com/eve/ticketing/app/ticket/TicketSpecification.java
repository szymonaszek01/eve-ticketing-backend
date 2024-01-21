package com.eve.ticketing.app.ticket;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class TicketSpecification {

    public static Specification<Ticket> ticketCodeEqual(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("code"), code);
    }

    public static Specification<Ticket> ticketFirstnameEqual(String firstname) {
        if (firstname == null || firstname.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("firstname"), firstname);
    }

    public static Specification<Ticket> ticketLastnameEqual(String lastname) {
        if (lastname == null || lastname.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("lastname"), lastname);
    }

    public static Specification<Ticket> ticketIdentityCardNumberEqual(String identityCardNumber) {
        if (identityCardNumber == null || identityCardNumber.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("identityCardNumber"), identityCardNumber);
    }

    public static Specification<Ticket> ticketCostBetween(Double minCost, Double maxCost) {
        if (minCost == null || maxCost == null || minCost > maxCost) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("cost"), BigDecimal.valueOf(minCost), BigDecimal.valueOf(maxCost));
    }
}
