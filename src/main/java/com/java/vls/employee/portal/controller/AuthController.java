package com.java.vls.employee.portal.controller;

import com.java.vls.employee.portal.entity.User;
import com.java.vls.employee.portal.jwt.JwtUtil;
import com.java.vls.employee.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            throw new Exception("Invalid credentials", e);
        }
        UserDetails userDetails = userService.loadUserByUsername(username);
        return jwtUtil.generateToken(userDetails.getUsername());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // Register and save the new user
        User registeredUser = userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully with ID: " + registeredUser.getId());
    }
}
