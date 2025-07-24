package com.gd.springecommerce.service;

import com.gd.springecommerce.model.User;
import com.gd.springecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public User registerUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean checkIfUserAlreadyExists(String email) {
        return userRepository.existsById(email);
    }
}
