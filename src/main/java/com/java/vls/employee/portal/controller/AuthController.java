package com.java.vls.employee.portal.controller;

import com.java.vls.employee.portal.entity.User;
import com.java.vls.employee.portal.jwt.JwtUtil;
import com.java.vls.employee.portal.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;


    private final JwtUtil jwtUtil;

    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password) throws Exception {
        try {
            log.info("Login attempt received: username={}", username);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            log.warn("Login failed: username={}, reason={}", username, e.getClass().getSimpleName());
            throw new Exception("Invalid credentials", e);
        }
        User userDetails = userService.findByUsername(username);
        log.info("Login successful: username={}", username);
        return jwtUtil.generateToken(userDetails.getUsername());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // Register and save the new user
        log.info("User registration requested: username={}", user.getUsername());
        User registeredUser = userService.registerUser(user);
        log.info("User registration completed: username={}, id={}", registeredUser.getUsername(), registeredUser.getId());
        return ResponseEntity.ok("User registered successfully with ID: " + registeredUser.getId());
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            log.warn("User lookup returned not found: username={}", username);
            return ResponseEntity.notFound().build();
        }
        log.info("User lookup successful: username={}, id={}", username, user.getId());
        return ResponseEntity.ok(user);
    }


}
