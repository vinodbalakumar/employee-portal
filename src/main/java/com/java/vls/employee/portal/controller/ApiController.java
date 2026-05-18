package com.java.vls.employee.portal.controller;

import com.java.vls.employee.portal.entity.TeslaTokens;
import com.java.vls.employee.portal.repository.TeslaTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private static final String TESLA_PUBLIC_KEY_RESOURCE =
            "static/.well-known/appspecific/com.tesla.3p.public-key.pem";

    private final TeslaTokenRepository teslaTokenRepository;

    public ApiController(TeslaTokenRepository teslaTokenRepository) {
        this.teslaTokenRepository = teslaTokenRepository;
    }

    /**
     * Sample protected endpoint used to verify API routing.
     */
    @GetMapping("/protected1")
    public ResponseEntity<String> getProtectedResource() {
        return ResponseEntity.ok("This is a protected resource!");
    }

    /**
     * Lightweight endpoint for confirming that the app is reachable.
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello, this is a secured endpoint!";
    }

    /**
     * Admin-only sample endpoint.
     */
    @GetMapping("/admin")
    public String admin() {
        log.info("Admin endpoint accessed");
        return "Hello Admin, this endpoint is secured and requires ADMIN role!";
    }

    /**
     * Receives the Tesla OAuth callback code and stores it for the local token flow.
     */
    @GetMapping("/callback")
    public String callback(@RequestParam String code) {
        log.info("Tesla authorization code received");
        TeslaTokens tokens = new TeslaTokens();
        tokens.setAccessToken(code);
        teslaTokenRepository.save(tokens);
        return "Tesla login successful. Code received: " + code;
    }

    /**
     * Serves the Tesla public key from the jar classpath for domain verification.
     */
    @GetMapping(
            value = "/.well-known/appspecific/com.tesla.3p.public-key.pem",
            produces = "text/plain"
    )
    public ResponseEntity<String> teslaPublicKey() throws IOException {
        // Load from the classpath so the file is available both in the IDE and in the Docker jar.
        ClassPathResource resource = new ClassPathResource(TESLA_PUBLIC_KEY_RESOURCE);
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String key = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline")
                .body(key);
    }

}
