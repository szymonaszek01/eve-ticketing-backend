package com.eve.ticketing.app.authuser;

import com.eve.ticketing.app.authuser.dto.RegisterDto;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthUserInitializer implements CommandLineRunner {

    private final AuthUserServiceImpl authUserService;

    @Override
    public void run(String... args) {
        log.info("Application has started generating \"AuthUser\" data");
        Faker faker = new Faker();

        // --- TEST ACCOUNT --- //
        RegisterDto registerDto = RegisterDto.builder()
                .email("jan.kowalski@gmail.com")
                .password("#Test1234")
                .firstname("Jan")
                .lastname("Kowalski")
                .phoneNumber("+48800800800")
                .build();
        authUserService.registerAuthUser(registerDto);
        // --- TEST ACCOUNT --- //

        for (int i = 0; i < 4; i++) {
            registerDto = RegisterDto.builder()
                    .email(faker.internet().emailAddress())
                    .password(faker.regexify("[a-zA-Z0-9@#$%^&+=]{8,20}"))
                    .firstname(faker.name().firstName())
                    .lastname(faker.name().lastName())
                    .phoneNumber("+48" + (8 - i) + "12345678")
                    .build();
            authUserService.registerAuthUser(registerDto);
        }

        log.info("Application has generated 5 records of \"AuthUser\" data");
    }
}
