package com.gd.springecommerce.service;

import com.gd.springecommerce.dto.CartProductDTO;
import com.gd.springecommerce.exception.InsufficientQuantityException;
import com.gd.springecommerce.exception.ProductNotFoundException;
import com.gd.springecommerce.model.Order;
import com.gd.springecommerce.model.Product;
import com.gd.springecommerce.repository.OrderRepository;
import com.gd.springecommerce.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;
    OrderRepository orderRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("product with id " + id + " does not exists"));
    }

    @Override
    public Product getProductIfValidToAddInCart(Long id, Integer quantity) {
        Product product = getProductById(id);
        if (product.getAvailable() >= quantity) {
            return product;
        }
        throw new InsufficientQuantityException("Out of stock for the product " + product.getTitle());
    }

    @Override
    @Transactional
    public Order checkOutProductsInTheCart(List<CartProductDTO> cart) {
        BigDecimal totalAmount = calculateTotalAmount(cart.stream().map(CartProductDTO::getSubTotal).toList());
        List<Product> productQuantities = productRepository.findQuantityByIds(cart.stream().map(CartProductDTO::getId).toList());
        Map<Long, Integer> idWithQuantity = productQuantities
                .stream()
                .collect(Collectors.toMap(Product::getId,Product::getAvailable));
        cart
                .forEach(cartProductDTO -> {
                    Long id = cartProductDTO.getId();
                    Integer updatedQuantity = idWithQuantity.get(id) - cartProductDTO.getQuantity();
                    productRepository.updateProductAvailability(updatedQuantity, id);
                });
        return orderRepository.save(Order.builder().total(totalAmount).build());
    }



    private BigDecimal calculateTotalAmount(List<BigDecimal> subTotals) {
        return subTotals
                .stream()
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

}
