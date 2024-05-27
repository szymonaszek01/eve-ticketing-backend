package com.eve.ticketing.app.authuser;

import com.eve.ticketing.app.authuser.dto.AuthUserFilterDto;
import com.eve.ticketing.app.authuser.dto.LoginDto;
import com.eve.ticketing.app.authuser.dto.RefreshTokenDto;
import com.eve.ticketing.app.authuser.dto.RegisterDto;
import com.eve.ticketing.app.authuser.exception.AuthUserProcessingException;
import com.eve.ticketing.app.authuser.exception.Error;
import com.eve.ticketing.app.authuser.security.JwtUtil;
import com.eve.ticketing.app.authuser.security.UserDetails;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.eve.ticketing.app.authuser.AuthUserSpecification.*;

@Slf4j
@Service
@AllArgsConstructor
public class AuthUserServiceImpl implements AuthUserService {

    private static final String USER = "USER";

    private static final String ADMIN = "ADMIN";

    private static final List<String> ADMIN_EMAIL_LIST = List.of("jan.kowalski@gmail.com");

    private final AuthUserRepository authUserRepository;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<AuthUser> getAuthUserList(int page, int size, AuthUserFilterDto authUserFilterDto) {
        Specification<AuthUser> authUserSpecification = Specification.where(AuthUserEmailEqual(authUserFilterDto.getEmail()))
                .and(AuthUserFirstnameEqual(authUserFilterDto.getFirstname()))
                .and(AuthUserLastnameEqual(authUserFilterDto.getLastname()))
                .and(AuthUserPhoneNumberEqual(authUserFilterDto.getPhoneNumber()));
        Pageable pageable = PageRequest.of(page, size);

        return authUserRepository.findAll(authUserSpecification, pageable);
    }

    @Override
    public AuthUser getAuthUserById(long id) throws AuthUserProcessingException {
        return authUserRepository.findById(id).orElseThrow(() -> {
            Error error = Error.builder().method("GET").field("id").value(id).description("id not found").build();
            log.error(error.toString());
            return new AuthUserProcessingException(HttpStatus.NOT_FOUND, error);
        });
    }

    @Override
    public AuthUser getAuthUserByEmail(String email) throws AuthUserProcessingException {
        return authUserRepository.findByEmail(email).orElseThrow(() -> {
            Error error = Error.builder().method("GET").field("email").value(email).description("email not found").build();
            log.error(error.toString());
            return new AuthUserProcessingException(HttpStatus.NOT_FOUND, error);
        });
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public AuthUser updateAuthUser(HashMap<String, Object> values) throws AuthUserProcessingException, ConstraintViolationException {
        Error error = Error.builder().method("PUT").build();
        if (values == null) {
            error.setField("");
            error.setValue("");
            error.setDescription("empty values");
            log.error(error.toString());
            throw new AuthUserProcessingException(HttpStatus.BAD_REQUEST, error);
        }
        if (values.get("id") == null || !(values.get("id") instanceof Number)) {
            error.setField("id");
            error.setValue(Objects.toString(values.get("id"), ""));
            error.setDescription("can not be null or has different type than Number");
            log.error(error.toString());
            throw new AuthUserProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        AuthUser authUser = getAuthUserById(((Number) values.remove("id")).longValue());
        Stream.of("created_at", "auth_token", "refresh_token", "role").forEach(values::remove);
        values.forEach((key, value) -> {
            String convertedKey = toCamelCase(key);
            try {
                Field field = authUser.getClass().getDeclaredField(convertedKey);
                field.setAccessible(true);
                error.setField(key);
                error.setValue(value);
                if (value instanceof String) {
                    if ("password".equals(convertedKey)) {
                        value = passwordEncoder.encode((String) value);
                    }
                    if (!"phoneNumber".equals(convertedKey) || isPhoneNumberValid((String) value)) {
                        field.set(authUser, value);
                    }
                }
            } catch (NullPointerException e) {
                error.setDescription("field can not be null");
                log.error(error.toString());
            } catch (NoSuchFieldException e) {
                error.setDescription("field does not exists");
                log.error(error.toString());
            } catch (IllegalAccessException e) {
                error.setDescription("illegal access to field");
                log.error(error.toString());
            }
        });

        return authUser;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteAuthUserById(long id) throws AuthUserProcessingException {
        try {
            authUserRepository.deleteById(id);
            log.info("AuthUser (id=\"{}\") was deleted", id);
        } catch (RuntimeException e) {
            Error error = Error.builder().method("DELETE").field("id").value(id).description("id not found").build();
            log.error(error.toString());
            throw new AuthUserProcessingException(HttpStatus.NOT_FOUND, error);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AuthUser registerAuthUser(RegisterDto registerDto) throws AuthUserProcessingException, ConstraintViolationException {
        Error error = Error.builder().method("POST").build();
        if (!isPhoneNumberValid(registerDto.getPhoneNumber())) {
            error.setField("phone_number");
            error.setValue(registerDto.getPhoneNumber());
            error.setDescription("phone number is invalid");
            log.error(error.toString());
            throw new AuthUserProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        UserDetails userDetails = getUserDetails(registerDto.getEmail(), registerDto.getPassword());
        if (userDetails != null) {
            error.setField("email");
            error.setValue(userDetails.getUsername());
            error.setDescription("email already in use");
            throw new AuthUserProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        AuthUser authUser = AuthUser.builder()
                .email(registerDto.getEmail())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .createdAt(new Date(System.currentTimeMillis()))
                .firstname(registerDto.getFirstname())
                .lastname(registerDto.getLastname())
                .phoneNumber(registerDto.getPhoneNumber())
                .role(ADMIN_EMAIL_LIST.contains(registerDto.getEmail()) ? ADMIN : USER)
                .build();
        authUserRepository.save(authUser);

        userDetails = new UserDetails(authUser.getId(), authUser.getEmail(), authUser.getPassword(), List.of(new SimpleGrantedAuthority(USER)));
        authUser.setAuthToken(jwtUtil.createToken(userDetails, 3 * 60 * 60 * 1000));
        authUser.setRefreshToken(jwtUtil.createToken(userDetails, 24 * 60 * 60 * 1000));

        return authUser;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AuthUser loginAuthUser(LoginDto loginDto) throws AuthUserProcessingException, ConstraintViolationException {
        UserDetails userDetails = getUserDetails(loginDto.getEmail(), loginDto.getPassword());
        if (userDetails == null) {
            Error error = Error.builder().method("POST").field("").value(loginDto).description("user not found").build();
            throw new AuthUserProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        AuthUser authUser = getAuthUserById(userDetails.getId());
        authUser.setAuthToken(jwtUtil.createToken(userDetails, 3 * 60 * 60 * 1000));
        authUser.setRefreshToken(jwtUtil.createToken(userDetails, 24 * 60 * 60 * 1000));

        return authUser;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AuthUser refreshToken(RefreshTokenDto refreshTokenDto) throws AuthUserProcessingException, ConstraintViolationException {
        AuthUser authUser = authUserRepository.findByIdAndRefreshToken(refreshTokenDto.getId(), refreshTokenDto.getRefreshToken()).orElseThrow(() -> {
            Error error = Error.builder().method("GET").field("").value(refreshTokenDto).description("invalid refresh token").build();
            log.error(error.toString());
            return new AuthUserProcessingException(HttpStatus.NOT_FOUND, error);
        });
        UserDetails userDetails = new UserDetails(authUser.getId(), authUser.getEmail(), authUser.getPassword(), List.of(new SimpleGrantedAuthority(USER)));
        authUser.setAuthToken(jwtUtil.createToken(userDetails, 3 * 60 * 60 * 1000));

        return authUser;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AuthUser validateToken(String token) throws AuthUserProcessingException {
        jwtUtil.validateToken(token);
        String email = jwtUtil.extractUsername(token);
        return getAuthUserByEmail(email);
    }

    private String toCamelCase(String value) {
        String[] parts = value.split("_");
        if (parts.length < 1) {
            return "";
        }

        StringBuilder camelCaseString = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            camelCaseString.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1).toLowerCase());
        }

        return camelCaseString.toString();
    }

    private boolean isPhoneNumberValid(String phoneNumberAsString) {
        try {
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phoneNumberAsString, "");
            return phoneNumberUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException e) {
            return false;
        }
    }

    private UserDetails getUserDetails(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            return (UserDetails) authentication.getPrincipal();
        } catch (BadCredentialsException e) {
            return null;
        }
    }
}
