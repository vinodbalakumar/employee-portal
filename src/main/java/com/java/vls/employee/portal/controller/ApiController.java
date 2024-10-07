package com.java.vls.employee.portal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping("/protected")
    public ResponseEntity<String> getProtectedResource() {
        return ResponseEntity.ok("This is a protected resource!");
    }

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
