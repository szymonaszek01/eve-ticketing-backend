package com.eve.ticketing.app.authuser;

import org.springframework.data.jpa.domain.Specification;

public class AuthUserSpecification {

    public static Specification<AuthUser> AuthUserEmailEqual(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("email"), email);
    }

    public static Specification<AuthUser> AuthUserFirstnameEqual(String firstname) {
        if (firstname == null || firstname.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("firstname"), firstname);
    }

    public static Specification<AuthUser> AuthUserLastnameEqual(String lastname) {
        if (lastname == null || lastname.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("lastname"), lastname);
    }

    public static Specification<AuthUser> AuthUserPhoneNumberEqual(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("phoneNumber"), phoneNumber);
    }
}
