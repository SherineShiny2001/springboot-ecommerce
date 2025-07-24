package com.gd.springecommerce.model;

import com.gd.springecommerce.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Builder.Default
    LocalDate orderDate = LocalDate.now();
    BigDecimal total;
    @Builder.Default
    OrderStatus orderStatus = OrderStatus.CONFIRMED;

}
