package com.java.vls.employee.portal.controller;

import com.java.vls.employee.portal.entity.TeslaTokens;
import com.java.vls.employee.portal.repository.TeslaTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);
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
        log.info("Protected sample endpoint requested");
        return ResponseEntity.ok("This is a protected resource!");
    }

    /**
     * Lightweight endpoint for confirming that the app is reachable.
     */
    @GetMapping("/hello")
    public String hello() {
        log.debug("Hello endpoint requested");
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
        log.info("Tesla authorization code received: codeLength={}", code.length());
        TeslaTokens tokens = new TeslaTokens();
        tokens.setAccessToken(code);
        TeslaTokens savedToken = teslaTokenRepository.save(tokens);
        log.info("Tesla authorization code stored: tokenId={}", savedToken.getId());
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
            log.error("Tesla public key file not found on classpath: resource={}", TESLA_PUBLIC_KEY_RESOURCE);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String key = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        log.info("Tesla public key served: resource={}, bytes={}", TESLA_PUBLIC_KEY_RESOURCE, key.length());

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline")
                .body(key);
    }

}
