package com.pichincha.spfmsaclientecoreservice;

import com.pichincha.spfmsaclientecoreservice.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SpringContextIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Test
    void contextLoads_andAccountServiceBeanPresent() {
        assertNotNull(accountService, "AccountService should be available in application context");
    }
}

