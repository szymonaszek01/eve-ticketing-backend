package com.eve.ticketing.app.authuser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long>, JpaSpecificationExecutor<AuthUser> {

    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findByIdAndRefreshToken(Long id, String refreshToken);
}
