package com.java.vls.employee.portal.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private User userUnderTest;

    @BeforeEach
    void setUp() {
        userUnderTest = new User("username", "password", "email","");
    }

    @Test
    void testIdGetterAndSetter() {
        final Long id = 0L;
        userUnderTest.setId(id);
        assertThat(userUnderTest.getId()).isEqualTo(id);
    }

    @Test
    void testUsernameGetterAndSetter() {
        final String username = "username";
        userUnderTest.setUsername(username);
        assertThat(userUnderTest.getUsername()).isEqualTo(username);
    }

    @Test
    void testPasswordGetterAndSetter() {
        final String password = "password";
        userUnderTest.setPassword(password);
        assertThat(userUnderTest.getPassword()).isEqualTo(password);
    }

    @Test
    void testEmailGetterAndSetter() {
        final String email = "email";
        userUnderTest.setEmail(email);
        assertThat(userUnderTest.getEmail()).isEqualTo(email);
    }
}
