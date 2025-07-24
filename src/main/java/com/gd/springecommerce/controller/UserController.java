package com.gd.springecommerce.controller;

import com.gd.springecommerce.model.EcommerceResponse;
import com.gd.springecommerce.model.User;
import com.gd.springecommerce.service.UserRegistrationServiceImpl;
import com.gd.springecommerce.validator.PasswordValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ecommerce/v1/users")
@Tag(name = "User Manager", description = "API for registration and login of the user")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserController {
    @Autowired
    UserRegistrationServiceImpl userRegistrationServiceImpl;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PasswordValidator passwordValidator;

    @Autowired
    AuthenticationManager authenticationManager;


    @Operation(summary = "Register a user", description = "Takes the valid details of user like email, password,and initiate user registration process")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully Registered"),
            @ApiResponse(responseCode = "400", description = "Error in the data to be inserted"),
            @ApiResponse(responseCode = "409",description = "User already exists")}
    )
    @PostMapping(path = "/register-user", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EcommerceResponse> registerUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        if (StringUtils.isNotBlank(user.getPassword())) {
            passwordValidator.validate(user.getPassword(), bindingResult);
        }
        if (!bindingResult.hasErrors()) {
            if (!userRegistrationServiceImpl.checkIfUserAlreadyExists(user.getEmail())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userRegistrationServiceImpl.registerUser(user);
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
            return new ResponseEntity<>(HttpStatus.valueOf(409));
        }
        Set<String> errorMessages = bindingResult.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toSet());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new EcommerceResponse(errorMessages));
    }

    @Operation(summary = "Login a user", description = "Takes email, password,and initiate user login process")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful Login"),
            @ApiResponse(responseCode = "409", description = "Bad Credentials")}
    )
    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,String>> loginUser(@RequestBody User user, HttpSession session) {
        try {
            String email = user.getEmail();
            String password = user.getPassword();
            Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
            authenticationManager.authenticate(authentication);
            Map<String, String> response = new HashMap<>();
            response.put("sessionId", session.getId());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
        }
    }
}
