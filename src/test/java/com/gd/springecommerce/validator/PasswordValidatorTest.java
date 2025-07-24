package com.gd.springecommerce.validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.*;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {

    private Errors errors;

    private static PasswordValidator passwordValidator;

    @BeforeAll
    static void beforeAll() {
        passwordValidator = new PasswordValidator();
    }


    @ParameterizedTest
    @ValueSource(strings = {"aA@1G", "aA@1aA@1aA@1aA@1"})
    void validatePassword_WithInvalidPasswordLength(String password) {
        errors = new MapBindingResult(new HashMap<>(), "password");
        passwordValidator.validate(password, errors);
        String errorCode = errors.getFieldError("password").getCode();
        assertTrue(errors.hasErrors());
        assertEquals(errorCode, "password.size");
    }

    @ParameterizedTest
    @ValueSource(strings = {"AA@1AA@1AA@1", "aa@1aa@1aa@1", "Aa@!Aa@!Aa@!", "Aa21Aa21Aa21"})
    void validatePassword_WithInvalidPasswordPattern(String password) {
        errors = new MapBindingResult(new HashMap<>(), "password");
        passwordValidator.validate(password, errors);
        String errorCode = errors.getFieldError("password").getCode();
        assertTrue(errors.hasErrors());
        assertEquals(errorCode, "password.pattern");
    }

    @Test
    void validatePassword_WithValidPassword() {
        String password = "Aa@1Aa@1Aa@1";
        errors = new MapBindingResult(new HashMap<>(), "password");
        passwordValidator.validate(password, errors);
        assertFalse(errors.hasErrors());
    }
}