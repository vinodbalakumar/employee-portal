package com.java.vinod.tesla.dashboard.services.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class JwtService {
    private final PublicKey publicKey;
    private final List<String> audiences;

    public JwtService(@Value("${app.jwt.public-key-base64}") String publicKeyBase64,
                      @Value("${app.jwt.public-key-path}") String publicKeyPath,
                      @Value("${app.jwt.audiences}") String audiences) {
        String publicPem = loadPem(publicKeyBase64, publicKeyPath);
        this.publicKey = parsePublicKey(publicPem);
        this.audiences = Arrays.stream(audiences.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
        if (this.audiences.isEmpty()) {
            throw new IllegalStateException("At least one JWT audience must be configured");
        }
    }

    public Claims parseClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (claims.getAudience().stream().noneMatch(audiences::contains)) {
            throw new JwtException("JWT audience " + claims.getAudience() + " is not allowed. Expected one of " + audiences);
        }
        return claims;
    }

    private String loadPem(String base64Pem, String keyPath) {
        try {
            if (base64Pem != null && !base64Pem.isBlank()) {
                return new String(Base64.getDecoder().decode(base64Pem), StandardCharsets.UTF_8);
            }
            if (keyPath != null && !keyPath.isBlank()) {
                return Files.readString(Path.of(keyPath), StandardCharsets.UTF_8);
            }
            throw new IllegalStateException("JWT_PUBLIC_KEY_BASE64 or JWT_PUBLIC_KEY_PATH must be configured");
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load JWT public key", exception);
        }
    }

    private PublicKey parsePublicKey(String pem) {
        try {
            String body = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(body);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT public key must be an RSA PUBLIC KEY PEM", exception);
        }
    }
}
