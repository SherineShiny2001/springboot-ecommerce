package com.gd.springecommerce.service;

import com.gd.springecommerce.model.User;

public interface UserRegistrationService {
    User registerUser(User user);
    boolean checkIfUserAlreadyExists(String email);
}
