package com.eve.ticketing.app.authuser;

import com.eve.ticketing.app.authuser.dto.*;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

import static com.eve.ticketing.app.authuser.AuthUserSpecification.*;
import static com.eve.ticketing.app.authuser.AuthUserUtil.getDateFromString;

@Slf4j
@Service
@AllArgsConstructor
public class AuthUserServiceImpl implements AuthUserService {

    private static final String USER = "USER";

    private static final String ADMIN = "ADMIN";

    private static final String LOCAL = "LOCAL";

    private static final String GOOGLE = "GOOGLE";

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    private static final List<String> ADMIN_EMAIL_LIST = List.of("jan.kowalski@gmail.com");

    private final AuthUserRepository authUserRepository;

    private final JwtUtil jwtUtil;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<AuthUser> getAuthUserList(int page, int size, AuthUserFilterDto authUserFilterDto, String[] sortArray) {
        if (page == 0 || size == 0) {
            return Page.empty();
        }
        Date minDate = getDateFromString(authUserFilterDto.getMinDate());
        Date maxDate = getDateFromString(authUserFilterDto.getMaxDate());
        Specification<AuthUser> authUserSpecification = Specification.where(AuthUserEmailEqual(authUserFilterDto.getEmail()))
                .and(authUserCreatedAtBetween(minDate, maxDate))
                .and(AuthUserFirstnameEqual(authUserFilterDto.getFirstname()))
                .and(AuthUserLastnameEqual(authUserFilterDto.getLastname()))
                .and(AuthUserPhoneNumberEqual(authUserFilterDto.getPhoneNumber()));

        List<String> allowedSortProperties = Stream.of("id", "createdAt").toList();
        List<String> allowedSortDirections = Stream.of(Sort.Direction.ASC.toString(), Sort.Direction.DESC.toString()).toList();
        if (!sortArray[0].contains(",")) {
            sortArray = new String[]{String.join(",", sortArray)};
        }
        List<Sort.Order> orderList = Arrays.stream(sortArray)
                .filter(sort -> {
                    String[] splitedSort = sort.split(",");
                    if (splitedSort.length < 2) {
                        return false;
                    }
                    return allowedSortProperties.contains(toCamelCase(splitedSort[0])) && allowedSortDirections.contains(splitedSort[1].toUpperCase());
                })
                .map(sort -> {
                    String[] splitedSort = sort.split(",");
                    return new Sort.Order(Sort.Direction.fromString(splitedSort[1]), toCamelCase(splitedSort[0]));
                })
                .toList();

        Pageable pageable = PageRequest.of(page, size, Sort.by(orderList));

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            values.put("id", userDetails.getId());
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
                boolean isUpdated = true;
                if (value instanceof String) {
                    if ("password".equals(convertedKey)) {
                        UserDetails userDetails = getUserDetails(authUser.getEmail(), (String) value);
                        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
                        if (userDetails != null || !((String) value).matches(pattern)) {
                            isUpdated = false;
                        } else {
                            value = passwordEncoder.encode((String) value);
                        }
                    }
                    if ("phoneNumber".equals(convertedKey) && isPhoneNumberNotValid((String) value)) {
                        isUpdated = false;
                    }
                    if (isUpdated) {
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
    public HashMap<String, Object> getAuthUserField(long id, String fieldName) throws AuthUserProcessingException {
        AuthUser authUser = getAuthUserById(id);
        Error error = Error.builder().method("GET").field(fieldName).build();
        String convertedKey = toCamelCase(fieldName);
        try {
            Field field = authUser.getClass().getDeclaredField(convertedKey);
            field.setAccessible(true);
            HashMap<String, Object> response = new HashMap<>(3);
            response.put("id", id);
            response.put("key", fieldName);
            response.put("value", field.get(authUser));
            return response;
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
        log.error(error.toString());
        throw new AuthUserProcessingException(HttpStatus.BAD_REQUEST, error);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AuthUser registerAuthUser(RegisterDto registerDto) throws AuthUserProcessingException, ConstraintViolationException {
        Error error = Error.builder().method("POST").build();
        if (isPhoneNumberNotValid(registerDto.getPhoneNumber())) {
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
            error.setDescription("email or password already in use");
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
                .authProvider(LOCAL)
                .build();

        try {
            authUserRepository.save(authUser);
        } catch (DataIntegrityViolationException e) {
            error.setField("email");
            error.setValue(authUser.getEmail());
            error.setDescription("email already in use");
            throw new AuthUserProcessingException(HttpStatus.CONFLICT, error);
        }

        userDetails = new UserDetails(authUser.getId(), authUser.getEmail(), authUser.getPassword(), List.of(new SimpleGrantedAuthority(authUser.getRole())));
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
    public AuthUser loginAuthUserViaGoogle(LoginViaGoogleDto loginViaGoogleDto) throws AuthUserProcessingException, ConstraintViolationException {
        Error error = Error.builder().method("POST").build();
        if (!isGoogleAccessTokenValid(loginViaGoogleDto.getAccessToken())) {
            error.setField("access_token");
            error.setValue(loginViaGoogleDto.getAccessToken());
            error.setDescription("access_token is invalid");
            log.error(error.toString());
            throw new AuthUserProcessingException(HttpStatus.BAD_REQUEST, error);
        }

        Optional<AuthUser> authUser = authUserRepository.findByEmail(loginViaGoogleDto.getEmail());
        if (authUser.isEmpty()) {
            if (isPhoneNumberNotValid(loginViaGoogleDto.getPhoneNumber())) {
                error.setField("phone_number");
                error.setValue(loginViaGoogleDto.getPhoneNumber());
                error.setDescription("phone number is invalid");
                log.error(error.toString());
                throw new AuthUserProcessingException(HttpStatus.BAD_REQUEST, error);
            }
            try {
                authUser = Optional.of(authUserRepository.save(AuthUser.builder()
                        .email(loginViaGoogleDto.getEmail())
                        .createdAt(new Date(System.currentTimeMillis()))
                        .firstname(loginViaGoogleDto.getFirstname())
                        .lastname(loginViaGoogleDto.getLastname())
                        .phoneNumber(loginViaGoogleDto.getPhoneNumber())
                        .role(ADMIN_EMAIL_LIST.contains(loginViaGoogleDto.getEmail()) ? ADMIN : USER)
                        .authProvider(GOOGLE)
                        .build()));
            } catch (DataIntegrityViolationException e) {
                error.setField("email");
                error.setValue(loginViaGoogleDto.getEmail());
                error.setDescription("email already in use");
                throw new AuthUserProcessingException(HttpStatus.CONFLICT, error);
            }
        }

        UserDetails userDetails = new UserDetails(authUser.get().getId(), authUser.get().getEmail(), authUser.get().getPassword(), List.of(new SimpleGrantedAuthority(authUser.get().getRole())));
        authUser.get().setAuthToken(jwtUtil.createToken(userDetails, 3 * 60 * 60 * 1000));
        authUser.get().setRefreshToken(jwtUtil.createToken(userDetails, 24 * 60 * 60 * 1000));

        return authUser.get();
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

    private boolean isPhoneNumberNotValid(String phoneNumberAsString) {
        try {
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phoneNumberAsString, "");
            return !phoneNumberUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException e) {
            return true;
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

    private boolean isGoogleAccessTokenValid(String accessToken) {
        try {
            URL url = new URL(GOOGLE_TOKEN_INFO_URL + "?access_token=" + accessToken);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader bufferedReader;
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                log.info("Google access token {} is valid", accessToken);
                bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                log.info("Google access token {} is not valid", accessToken);
                bufferedReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            String currentLine;
            StringBuilder body = new StringBuilder();
            while ((currentLine = bufferedReader.readLine()) != null) {
                body.append(currentLine);
            }
            log.info("Google account data {} associated with provided token.", body);
            return responseCode == 200;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }
}
