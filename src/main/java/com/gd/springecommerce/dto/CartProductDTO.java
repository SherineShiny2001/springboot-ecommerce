package com.gd.springecommerce.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CartProductDTO implements Serializable {
    transient Long id;
    String title;
    Integer quantity;
    BigDecimal subTotal;
}
