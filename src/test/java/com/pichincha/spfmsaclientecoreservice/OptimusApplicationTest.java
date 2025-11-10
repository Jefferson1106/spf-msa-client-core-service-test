package com.pichincha.spfmsaclientecoreservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for OptimusApplication main class.
 * Tests the application startup and Spring Boot configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
public class OptimusApplicationTest {

    /**
     * Test that verifies the Spring Boot application context loads successfully.
     * This is a smoke test to ensure the application can start without errors.
     */
    @Test
    public void contextLoads() {
        // This test will pass if the Spring context loads without exceptions
        // No explicit assertions needed - Spring Boot will fail the test if context cannot load
        assertTrue(true, "Context loaded successfully");
    }

    /**
     * Test that verifies the application main class is properly configured.
     * This ensures the @SpringBootApplication annotation is working correctly.
     */
    @Test
    public void applicationMainClassExists() {
        // Verify the main class exists and has the correct structure
        assertTrue(OptimusApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class),
                "OptimusApplication should have @SpringBootApplication annotation");
    }

    /**
     * Test that verifies the main method exists and is properly defined.
     */
    @Test
    public void mainMethodExists() throws NoSuchMethodException {
        // Verify the main method exists with correct signature
        OptimusApplication.class.getDeclaredMethod("main", String[].class);
        assertTrue(true, "Main method exists with correct signature");
    }
}
