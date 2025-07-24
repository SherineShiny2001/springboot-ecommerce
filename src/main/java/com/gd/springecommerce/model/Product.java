package com.gd.springecommerce.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Data
@JsonInclude
@Builder
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Title is mandatory")
    @Column(nullable = false, length = 50)
    private String title;
    @NotNull(message = "Available quantity is mandatory")
    @Column(nullable = false, length = 10)
    private Integer available;
    @NotNull(message = "Price is mandatory")
    @Column(nullable = false, length = 15)
    private BigDecimal price;
}
