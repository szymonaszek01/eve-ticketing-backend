package com.eve.ticketing.app.emailnotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EmailNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailNotificationApplication.class, args);
    }
}
