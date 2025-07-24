package com.gd.springecommerce.service;

import com.gd.springecommerce.dto.CartProductDTO;
import com.gd.springecommerce.model.Order;
import com.gd.springecommerce.model.Product;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Long id);
    Product getProductIfValidToAddInCart(Long id, Integer quantity);
    Order checkOutProductsInTheCart(List<CartProductDTO> cart);
}
