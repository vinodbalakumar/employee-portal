package com.java.vls.employee.portal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SecuredController {

    // Accessible to authenticated users
    @GetMapping("/hello")
    public String hello() {
        return "Hello, this is a secured endpoint!";
    }

    // Accessible to authenticated users with ADMIN role
    @GetMapping("/admin")
    public String admin() {
        return "Hello Admin, this endpoint is secured and requires ADMIN role!";
    }
}
