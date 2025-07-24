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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Test
    void loadByUserName_ExistingUser() {
        String email = "abc@gmail.com";
        User user = User
                .builder()
                .email(email)
                .password("abc@123")
                .build();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"      ", "abc@gmail.com"})
    void loadByUserName_NonExistingUser(String email) {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));
    }
}