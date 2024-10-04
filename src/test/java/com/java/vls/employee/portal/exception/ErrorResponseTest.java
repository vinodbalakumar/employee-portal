package com.java.vls.employee.portal.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    private ErrorResponse errorResponseUnderTest;

    @BeforeEach
    void setUp() {
        errorResponseUnderTest = new ErrorResponse("message", List.of("value"));
    }

    @Test
    void testMessageGetterAndSetter() {
        final String message = "message";
        errorResponseUnderTest.setMessage(message);
        assertThat(errorResponseUnderTest.getMessage()).isEqualTo(message);
    }

    @Test
    void testDetailsGetterAndSetter() {
        final List<String> details = List.of("value");
        errorResponseUnderTest.setDetails(details);
        assertThat(errorResponseUnderTest.getDetails()).isEqualTo(details);
    }
}
