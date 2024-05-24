package com.eve.ticketing.app.authuser.security;

import com.eve.ticketing.app.authuser.exception.AuthUserProcessingException;
import com.eve.ticketing.app.authuser.exception.Error;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final String SECRET_KEY = "test123";

    private final UserDetailsService userDetailsService;

    public String createToken(UserDetails userDetails, long expirationTime) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", List.of(userDetails.getAuthorities()));
        return Jwts.builder().setClaims(claims).setSubject(userDetails.getUsername()).setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis() + expirationTime)).signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
    }

    public void validateToken(String token) throws AuthUserProcessingException {
        Error error = Error.builder().method("").field("auth_token").value(token).build();
        UserDetails userDetails = null;
        try {
            userDetails = (UserDetails) userDetailsService.loadUserByUsername(extractUsername(token));
        } catch (UsernameNotFoundException e) {
            error.setDescription("email not found");
        } catch (SignatureException e) {
            error.setDescription("invalid jwt token signature");
        } catch (MalformedJwtException e) {
            error.setDescription("invalid jwt token");
        } catch (ExpiredJwtException e) {
            error.setDescription("jwt token is expired");
        } catch (UnsupportedJwtException e) {
            error.setDescription("jwt token is unsupported");
        } catch (IllegalArgumentException e) {
            error.setDescription("invalid jwt token claims");
        }

        if (userDetails == null || error.getDescription() != null) {
            log.error(error.toString());
            throw new AuthUserProcessingException(HttpStatus.BAD_REQUEST, error);
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }
}
