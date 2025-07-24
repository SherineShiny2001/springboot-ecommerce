package com.gd.springecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ProductDTO {
    @NotNull(message = "Id is mandatory")
    Long id;
    @NotNull(message = "quantity is mandatory")
    @Min(value = 1, message = "Add at least 1no")
    Integer quantity;
}
