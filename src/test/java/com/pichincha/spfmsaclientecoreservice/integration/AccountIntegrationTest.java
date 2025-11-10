package com.pichincha.spfmsaclientecoreservice.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AccountIntegrationTest {

    @Test
    @DisplayName("Integration test - Spring context loads successfully")
    void contextLoads() {
        // This test ensures that the Spring application context loads correctly
        // If any beans have configuration issues, this test will fail
        assertThat(true).isTrue();
    }
}
