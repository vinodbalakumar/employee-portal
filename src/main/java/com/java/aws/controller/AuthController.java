package com.java.aws.controller;


import com.java.aws.entity.User;
import com.java.aws.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public UserDetails login() {
        return userService.loadUserByUsername("vinod");
    }

    @GetMapping("/test")
    public String test() {
        return "Test";
    }
}
