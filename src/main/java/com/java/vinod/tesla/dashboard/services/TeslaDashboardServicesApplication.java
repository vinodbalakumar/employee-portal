package com.java.vinod.tesla.dashboard.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableAsync
public class TeslaDashboardServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeslaDashboardServicesApplication.class, args);
    }
}
