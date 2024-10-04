package com.java.vls.employee.portal.exception;

import org.junit.jupiter.api.BeforeEach;

class MissingHeaderExceptionTest {

    private MissingHeaderException missingHeaderExceptionUnderTest;

    @BeforeEach
    void setUp() {
        missingHeaderExceptionUnderTest = new MissingHeaderException("exception");
    }
}
