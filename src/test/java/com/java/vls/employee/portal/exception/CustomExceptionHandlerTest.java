package com.java.vls.employee.portal.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class CustomExceptionHandlerTest {

    private CustomExceptionHandler customExceptionHandlerUnderTest;

    @BeforeEach
    void setUp() {
        customExceptionHandlerUnderTest = new CustomExceptionHandler();
    }

    @Test
    void testHandleHeaderException() {
        // Setup
        // Run the test
        final ResponseEntity<Object> result = customExceptionHandlerUnderTest.handleHeaderException(new Exception("message"), null);

        // Verify the results
    }

    @Test
    void testHandleAllExceptions() {
        // Setup
        // Run the test
        final ResponseEntity<Object> result = customExceptionHandlerUnderTest.handleAllExceptions(new Exception("message"), null);

        // Verify the results
    }
}
