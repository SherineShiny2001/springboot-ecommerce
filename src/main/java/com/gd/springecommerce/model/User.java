package com.gd.springecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class User {
    @Id
    @NotBlank(message = "Email is mandatory")
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", message = "Please enter a valid email address in the format example@example.com")
    @Column(nullable = false, length = 30)
    private String email;
    @NotBlank(message = "Password is mandatory")
    @Column(nullable = false, length = 80)
    private String password;
}
