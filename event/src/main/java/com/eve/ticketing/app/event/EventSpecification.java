package com.eve.ticketing.app.event;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Date;

public class EventSpecification {

    public static Specification<Event> eventNameEqual(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), name);
    }

    public static Specification<Event> eventUnitPriceBetween(Double minUnitPrice, Double maxUnitPrice) {
        if (minUnitPrice == null || maxUnitPrice == null || minUnitPrice > maxUnitPrice) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("unitPrice"), BigDecimal.valueOf(minUnitPrice), BigDecimal.valueOf(maxUnitPrice));
    }

    public static Specification<Event> eventStartAtBetween(Date minDate, Date maxDate) {
        if (minDate == null || maxDate == null || minDate.after(maxDate)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("startAt"), minDate, maxDate);
    }

    public static Specification<Event> eventEndAtBetween(Date minDate, Date maxDate) {
        if (minDate == null || maxDate == null || minDate.after(maxDate)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("endAt"), minDate, maxDate);
    }

    public static Specification<Event> eventCountryEqual(String country) {
        if (country == null || country.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("country"), country);
    }

    public static Specification<Event> eventAddressEqual(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("address"), address);
    }
}
