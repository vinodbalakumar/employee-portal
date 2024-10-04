package com.java.vls.employee.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EmployeePortalApp {

    public static void main(String[] args) {
        SpringApplication.run(EmployeePortalApp.class, args);
    }
}
