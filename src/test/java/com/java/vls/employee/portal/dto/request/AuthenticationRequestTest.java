package com.java.vls.employee.portal.dto.request;

import org.junit.jupiter.api.BeforeEach;

class AuthenticationRequestTest {

    private AuthenticationRequest authenticationRequestUnderTest;

    @BeforeEach
    void setUp() {
        authenticationRequestUnderTest = new AuthenticationRequest("username", "password");
    }
}
