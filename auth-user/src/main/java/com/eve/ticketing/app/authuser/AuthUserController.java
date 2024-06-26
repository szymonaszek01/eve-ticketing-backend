package com.eve.ticketing.app.authuser;

import com.eve.ticketing.app.authuser.dto.AuthUserFilterDto;
import com.eve.ticketing.app.authuser.dto.LoginDto;
import com.eve.ticketing.app.authuser.dto.RefreshTokenDto;
import com.eve.ticketing.app.authuser.dto.RegisterDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Tag(name = "Auth user", description = "Auth user management APIs")
@RequestMapping("/api/v1/auth-user")
@RequiredArgsConstructor
@RestController
public class AuthUserController {

    private final AuthUserServiceImpl authUserService;

    @GetMapping("/all")
    public ResponseEntity<?> getAuthUserList(@RequestParam(value = "page") int page,
                                             @RequestParam(value = "size") int size,
                                             @RequestParam(defaultValue = "id,desc") String[] sort,
                                             AuthUserFilterDto authUserFilterDto) {
        Page<AuthUser> authUserPage = authUserService.getAuthUserList(page, size, authUserFilterDto, sort);
        return new ResponseEntity<>(authUserPage, HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getAuthUserById(@PathVariable long id) {
        AuthUser authUser = authUserService.getAuthUserById(id);
        return new ResponseEntity<>(authUser, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateAuthUser(@RequestBody HashMap<String, Object> values) {
        AuthUser authUser = authUserService.updateAuthUser(values);
        return new ResponseEntity<>(authUser, HttpStatus.OK);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteAuthUserById(@PathVariable long id) {
        authUserService.deleteAuthUserById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAuthUser(@Valid @RequestBody RegisterDto registerDto) {
        AuthUser authUser = authUserService.registerAuthUser(registerDto);
        return new ResponseEntity<>(authUser, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginAuthUser(@Valid @RequestBody LoginDto loginDto) {
        AuthUser authUser = authUserService.loginAuthUser(loginDto);
        return new ResponseEntity<>(authUser, HttpStatus.OK);
    }

    @PutMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
        AuthUser authUser = authUserService.refreshToken(refreshTokenDto);
        return new ResponseEntity<>(authUser, HttpStatus.OK);
    }

    @GetMapping("/validate-token/{token}")
    public ResponseEntity<?> validateToken(@PathVariable String token) {
        AuthUser authUser = authUserService.validateToken(token);
        return new ResponseEntity<>(authUser, HttpStatus.OK);
    }
}
