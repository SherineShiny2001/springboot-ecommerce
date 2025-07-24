package com.gd.springecommerce.service;

import com.gd.springecommerce.model.User;
import com.gd.springecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserRegistrationServiceImpl userRegistrationService;

    @Test
    void registerUser() {
        User user = User.builder()
                .email("abc@gmail.com")
                .password("abc@123")
                .build();
        when(userRepository.save(any())).thenReturn(user);
        User savedUser = userRegistrationService.registerUser(user);
        assertNotNull(savedUser);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"       ", "abc@gmail.com"})
    void checkIfUserAlreadyExists_NonExistingUser(String email) {
        when(userRepository.existsById(any())).thenReturn(false);
        boolean isUserExists = userRegistrationService.checkIfUserAlreadyExists(email);
        assertFalse(isUserExists);
    }

    @Test
    void checkIfUserAlreadyExists_ExistingUser() {
        when(userRepository.existsById(any())).thenReturn(true);
        boolean isUserExists = userRegistrationService.checkIfUserAlreadyExists("abc@gmail.com");
        assertTrue(isUserExists);
    }
}