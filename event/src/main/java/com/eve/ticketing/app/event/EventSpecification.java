package com.eve.ticketing.app.event;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Date;

public class EventSpecification {

    public static Specification<Event> eventNameEqual(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.upper(root.get("name")), "%" + name.toUpperCase() + "%");
    }

    public static Specification<Event> eventUnitPriceBetween(Double minUnitPrice, Double maxUnitPrice) {
        if (minUnitPrice == null && maxUnitPrice == null) {
            return null;
        }
        double finalMinUnitPrice = (minUnitPrice == null) ? 0 : minUnitPrice;
        double finalMaxUnitPrice = (maxUnitPrice == null) ? 0 : maxUnitPrice;
        if (finalMinUnitPrice < 0 || finalMaxUnitPrice < 0 || finalMinUnitPrice > finalMaxUnitPrice) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("unitPrice"), BigDecimal.valueOf(finalMinUnitPrice), BigDecimal.valueOf(finalMaxUnitPrice));
    }

    public static Specification<Event> eventStartAtBetween(Date minDate, Date maxDate) {
        if (minDate == null && maxDate == null) {
            return null;
        }
        Date finalMinDate = (minDate == null) ? new Date(System.currentTimeMillis()) : minDate;
        Date finalMaxDate = (maxDate == null) ? new Date(System.currentTimeMillis()) : maxDate;
        if (finalMinDate.after(finalMaxDate)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("startAt"), finalMinDate, finalMaxDate);
    }

    public static Specification<Event> eventEndAtBetween(Date minDate, Date maxDate) {
        if (minDate == null || maxDate == null || minDate.after(maxDate)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("endAt"), minDate, maxDate);
    }

    public static Specification<Event> eventCountryEqual(String country) {
        if (StringUtils.isBlank(country)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("country"), country);
    }

    public static Specification<Event> eventAddressEqual(String address) {
        if (StringUtils.isBlank(address)) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("address"), address);
    }
}
