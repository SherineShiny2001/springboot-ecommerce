package com.gd.springecommerce.validator;

import com.gd.springecommerce.model.User;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PasswordValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        String password = (String) target;
        if (password.length() < 6 || password.length() > 15) {
            errors.rejectValue("password","password.size","Password should be minimum 6 characters and maximum 15 characters");
        }
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            errors.rejectValue("password","password.pattern","Password should contain uppercase, lowercase, numbers and special characters");
        }
    }
}
