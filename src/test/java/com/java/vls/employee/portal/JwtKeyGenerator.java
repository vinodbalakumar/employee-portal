package com.java.vls.employee.portal;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generate a strong key for HS256
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Base64 Encoded Secret Key: " + encodedKey);
    }
}
