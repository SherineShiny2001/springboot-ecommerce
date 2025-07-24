package com.gd.springecommerce.service;

import com.gd.springecommerce.dto.CartProductDTO;
import com.gd.springecommerce.exception.InsufficientQuantityException;
import com.gd.springecommerce.exception.ProductNotFoundException;
import com.gd.springecommerce.model.Order;
import com.gd.springecommerce.model.Product;
import com.gd.springecommerce.repository.OrderRepository;
import com.gd.springecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void testGetAllProducts_WhenNoProductsInDb() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());
        List<Product> products = productService.getAllProducts();
        assertNotNull(products);
        assertTrue(products.isEmpty());
        Mockito.verify(productRepository, Mockito.times(1)).findAll();
    }

    @Test
    void testGetAllProducts_WithProductsInDb() {
        Product product1 = Product.builder().build();
        Product product2 = Product.builder().build();
        List<Product> productList = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(productList);
        List<Product> products = productService.getAllProducts();
        assertNotNull(products);
        assertEquals(products.size(), 2);
        Mockito.verify(productRepository, Mockito.times(1)).findAll();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = 897L)
    void getProductById_WithInvalidId(Long id) {
        Optional<Product> product = Optional.empty();
        when(productRepository.findById(id)).thenReturn(product);
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(id));
    }

    @Test
    void getProductById_WithValidId() {
        Product productFromDb = Product.builder().build();
        Optional<Product> product = Optional.of(productFromDb);
        when(productRepository.findById(Mockito.anyLong())).thenReturn(product);
        Product fetchedProduct = productService.getProductById(123L);
        assertEquals(productFromDb, fetchedProduct);
    }

    @Test
    void getProductIfValidToAddInCart_WithInsufficientQuantity() {
        Product productFromDb = Product.builder().build();
        productFromDb.setAvailable(10);
        Optional<Product> product = Optional.of(productFromDb);
        when(productRepository.findById(Mockito.anyLong())).thenReturn(product);
        assertThrows(InsufficientQuantityException.class, () -> productService.getProductIfValidToAddInCart(123L, 15));
        Mockito.verify(productRepository, Mockito.times(1)).findById(123L);

    }

    @Test
    void getProductIfValidToAddInCart_WithSufficientQuantity() {
        Product productFromDb = Product.builder().build();
        productFromDb.setAvailable(10);
        Optional<Product> product = Optional.of(productFromDb);
        when(productRepository.findById(Mockito.anyLong())).thenReturn(product);
        Product fetchedProduct = productService.getProductIfValidToAddInCart(123L, 5);
        assertNotNull(fetchedProduct);
        assertEquals(productFromDb, fetchedProduct);
        Mockito.verify(productRepository, Mockito.times(1)).findById(123L);

    }
    @Test
    void checkOutProductsInTheCart_WithValidProducts() {
        CartProductDTO cartProductDTO1 = CartProductDTO.builder()
                .id(1L)
                .title("product 1")
                .quantity(2)
                .subTotal(BigDecimal.valueOf(250L))
                .build();

        CartProductDTO cartProductDTO2 = CartProductDTO.builder()
                .id(2L)
                .title("product 2")
                .quantity(3)
                .subTotal(BigDecimal.valueOf(150L))
                .build();

        List<CartProductDTO> cartProductDTOS = Arrays.asList(cartProductDTO1, cartProductDTO2);
        Product product1 = Product
                .builder()
                .id(1L)
                .title("product 1")
                .available(5)
                .price(BigDecimal.valueOf(125))
                .build();

        Product product2 = Product
                .builder()
                .id(2L)
                .title("product 2")
                .available(7)
                .price(BigDecimal.valueOf(75))
                .build();
        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findQuantityByIds(anyList())).thenReturn(products);
        doNothing().when(productRepository).updateProductAvailability(any(), any());
        Order order = Order
                .builder()
                .total(BigDecimal.valueOf(400))
                .build();
        when(orderRepository.save(any())).thenReturn(order);
        Order placedOrder = productService.checkOutProductsInTheCart(cartProductDTOS);
        product1.setAvailable(product1.getAvailable() - cartProductDTO1.getQuantity());
        product2.setAvailable(product2.getAvailable() - cartProductDTO2.getQuantity());
        assertEquals(BigDecimal.valueOf(400), placedOrder.getTotal());
        assertEquals(product1.getAvailable(), 3);
        assertEquals(product2.getAvailable(), 4);
    }
}
