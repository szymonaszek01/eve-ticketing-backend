package com.eve.ticketing.app.authuser;

import com.eve.ticketing.app.authuser.dto.*;
import com.eve.ticketing.app.authuser.exception.AuthUserProcessingException;
import com.eve.ticketing.app.authuser.security.UserDetails;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.domain.Page;

import java.util.HashMap;

public interface AuthUserService {

    Page<AuthUser> getAuthUserList(int page, int size, AuthUserFilterDto AuthUserFilterDto, String[] sortArray);

    AuthUser getAuthUserById(long id) throws AuthUserProcessingException;

    AuthUser updateAuthUser(HashMap<String, Object> values) throws AuthUserProcessingException, ConstraintViolationException;

    void deleteAuthUserById(long id) throws AuthUserProcessingException;

    AuthUser getAuthUserByEmail(String email) throws AuthUserProcessingException;

    AuthUser registerAuthUser(RegisterDto registerDto) throws AuthUserProcessingException, ConstraintViolationException;

    AuthUser loginAuthUser(LoginDto loginDto) throws AuthUserProcessingException, ConstraintViolationException;

    AuthUser loginAuthUserViaGoogle(LoginViaGoogleDto loginViaGoogleDto) throws AuthUserProcessingException, ConstraintViolationException;

    AuthUser refreshToken(RefreshTokenDto refreshTokenDto) throws AuthUserProcessingException, ConstraintViolationException;

    AuthUser validateToken(String token) throws AuthUserProcessingException;
}
