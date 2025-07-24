package com.gd.springecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.springecommerce.model.User;
import com.gd.springecommerce.service.UserRegistrationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@TestPropertySource(properties = {"spring.sql.init.mode=never"})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRegistrationServiceImpl userRegistrationServiceImpl;


    @MockBean
    private AuthenticationManager authenticationManager;


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"        ", "invalid_email"})
    void registerUser_WithInvalidEmail(String email) throws Exception {
        User user = User.builder()
                .email(email)
                .password("Abc@123")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/ecommerce/v1/users/register-user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"        ", "Aa@1", "Aa@Aa@Aa@Aa@Aa@Aa@1", "a1@a1@a1@", "A1@A1@A1@", "Aa@Aa@Aa@", "Aa1Aa1Aa1"})
    void registerUser_WithInvalidPassword(String password) throws Exception {
        User user = User.builder()
                .email("abc@gmail.com")
                .password(password)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/ecommerce/v1/users/register-user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_ForExistingUser() throws Exception {
        User user = User.builder()
                .email("sample@gmail.com")
                .password("AaBbCc@&123")
                .build();

        when(userRegistrationServiceImpl.checkIfUserAlreadyExists(anyString())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/ecommerce/v1/users/register-user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict());
        verify(userRegistrationServiceImpl, only()).checkIfUserAlreadyExists(anyString());
        verify(userRegistrationServiceImpl, times(0)).registerUser(any());
    }

    @Test
    void registerUser_ForNonExistingUser() throws Exception {
        User user = User.builder()
                .email("sample@gmail.com")
                .password("AaBbCc@&123")
                .build();

        when(userRegistrationServiceImpl.checkIfUserAlreadyExists(anyString())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/ecommerce/v1/users/register-user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());
        verify(userRegistrationServiceImpl, times(1)).checkIfUserAlreadyExists(anyString());
        verify(userRegistrationServiceImpl, times(1)).registerUser(any());
    }

    @Test
    void loginUser_ForInvalidUser() throws Exception {
        User user = User.builder()
                .email("sample@gmail.com")
                .password("AaBbCc@&123")
                .build();

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));
        mockMvc.perform(MockMvcRequestBuilders.post("/ecommerce/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUser_ForValidUser() throws Exception {
        User user = User.builder()
                .email("sample@gmail.com")
                .password("AaBbCc@&123")
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(null, null));
        mockMvc.perform(MockMvcRequestBuilders.post("/ecommerce/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }
}