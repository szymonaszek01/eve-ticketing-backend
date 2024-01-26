package com.eve.ticketing.app.smsnotification;

import org.springframework.data.jpa.domain.Specification;

public class SmsNotificationSpecification {

    public static Specification<SmsNotification> smsNotificationPhoneNumberEqual(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("phoneNumber"), phoneNumber);
    }

    public static Specification<SmsNotification> smsNotificationMessageEqual(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("message"), message);
    }
}
