package com.eve.ticketing.app.authuser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthUserApplication.class, args);
    }
}
