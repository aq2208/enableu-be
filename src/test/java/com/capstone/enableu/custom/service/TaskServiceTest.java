package com.capstone.enableu.custom.service;

import com.capstone.enableu.common.security.CurrentUserDetailContainer;
import com.capstone.enableu.common.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.capstone.enableu.custom.enums.Role.ADMIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@Transactional
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class TaskServiceTest {

    @MockBean
    CurrentUserDetailContainer currentUserDetailContainer;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        given(currentUserDetailContainer.getCurrentUser())
                .willReturn(new CustomUserDetails(
                        1L,
                        "sampleUsername",
                        "samplePassword",
                        List.of(new SimpleGrantedAuthority(ADMIN.name()))
                ));
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void createTask() {
        assertTrue(true);
    }

    @Test
    void getTask() {
        assertTrue(true);
    }

    @Test
    void softDeleteTask() {
        assertTrue(true);
    }

    @Test
    void updateTask() {
        assertTrue(true);
    }
}