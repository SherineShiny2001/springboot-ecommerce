package com.gd.springecommerce.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@Data
@JsonInclude
public class EcommerceResponse<T> {
    private Set<String> errors;
    private T successResponse;

    public EcommerceResponse(Set<String> errors) {
        this.errors = errors;
    }

    public EcommerceResponse(T successResponse) {
        this.successResponse = successResponse;
    }
}
