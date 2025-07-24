package com.gd.springecommerce.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.springecommerce.dto.CartProductDTO;
import com.gd.springecommerce.dto.ProductDTO;
import com.gd.springecommerce.model.Order;
import com.gd.springecommerce.model.Product;
import com.gd.springecommerce.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"spring.sql.init.mode=never"})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductServiceImpl productService;
    private MockHttpSession mockHttpSession;

    @BeforeEach
    void setUp() {
        mockHttpSession = new MockHttpSession();
    }

    @Test
    void getAllProducts_WhenInventoryIsEmpty() throws Exception {
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders.get("/ecommerce/v1/products/all-products")
                        .content(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getAllProducts_WhenInventoryIsNotEmpty() throws Exception {
        when(productService.getAllProducts()).thenReturn(Collections.singletonList(Product.builder().build()));
        mockMvc.perform(MockMvcRequestBuilders.get("/ecommerce/v1/products/all-products")
                        .content(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
        verify(productService, times(1)).getAllProducts();
    }

    @ParameterizedTest
    @MethodSource("idQuantityArgumentProvider")
    void addItemsToCart_WithInvalidProduct(Long id, Integer quantity) throws Exception {
        ProductDTO productDTO = ProductDTO.builder().id(id).quantity(quantity).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/ecommerce/v1/products/cart")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }

    @Test
    void addItemsToCart_WithValidProduct() throws Exception {
        ProductDTO productDTO = ProductDTO.builder().id(1L).quantity(1).build();
        Product product = Product.builder().price(BigDecimal.TEN).build();
        when(productService.getProductIfValidToAddInCart(anyLong(), anyInt())).thenReturn(product);
        mockMvc.perform(MockMvcRequestBuilders.post("/ecommerce/v1/products/cart")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk());
        verify(productService, times(1)).getProductIfValidToAddInCart(anyLong(), anyInt());
    }

    @Test
    void displayCart_WhenCartIsEmpty() throws Exception {
        String json = mockMvc.perform(MockMvcRequestBuilders.get("/ecommerce/v1/products/cart/products")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<CartProductDTO> cartProductDTOS = objectMapper.readValue(json, new TypeReference<>(){});
        assertTrue(cartProductDTOS.isEmpty());
    }

    @Test
    void displayCart_WhenCartIsNotEmpty() throws Exception {
        CartProductDTO cartProductDTO1 = CartProductDTO.builder().build();
        CartProductDTO cartProductDTO2 = CartProductDTO.builder().build();
        List<CartProductDTO> cartProductDTOS = Arrays.asList(cartProductDTO1, cartProductDTO2);
        mockHttpSession.setAttribute("cart", cartProductDTOS);
        String json = mockMvc.perform(MockMvcRequestBuilders.get("/ecommerce/v1/products/cart/products")
                        .session(mockHttpSession)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<CartProductDTO> retrievedCart = objectMapper.readValue(json, new TypeReference<>(){});
        assertFalse(retrievedCart.isEmpty());
        assertEquals(cartProductDTOS.size(), retrievedCart.size());
    }

    @Test
    void removeProductFromCart_WhenCartIsEmpty() throws Exception {
        String json = mockMvc.perform(MockMvcRequestBuilders.delete("/ecommerce/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<CartProductDTO> cartProductDTOS = objectMapper.readValue(json, new TypeReference<>(){});
        assertTrue(cartProductDTOS.isEmpty());
    }

    @Test
    void removeProductFromCart_WhenProductIsAvailable() throws Exception {
        CartProductDTO cartProductDTO1 = CartProductDTO.builder().id(1L).build();
        CartProductDTO cartProductDTO2 = CartProductDTO.builder().id(2L).build();
        List<CartProductDTO> cartProductDTOS = new ArrayList<>();
        cartProductDTOS.add(cartProductDTO1);
        cartProductDTOS.add(cartProductDTO2);
        int originalCartSize = cartProductDTOS.size();;
        mockHttpSession.setAttribute("cart", cartProductDTOS);
        String json = mockMvc.perform(MockMvcRequestBuilders.delete("/ecommerce/v1/products/1")
                        .session(mockHttpSession)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<CartProductDTO> retrievedCart = objectMapper.readValue(json, new TypeReference<>(){});
        assertFalse(retrievedCart.isEmpty());
        assertEquals(originalCartSize - 1, retrievedCart.size());
    }

    @Test
    void removeProductFromCart_WhenProductIsNotAvailable() throws Exception {
        CartProductDTO cartProductDTO1 = CartProductDTO.builder().id(1L).build();
        CartProductDTO cartProductDTO2 = CartProductDTO.builder().id(2L).build();
        List<CartProductDTO> cartProductDTOS = new ArrayList<>();
        cartProductDTOS.add(cartProductDTO1);
        cartProductDTOS.add(cartProductDTO2);
        int originalCartSize = cartProductDTOS.size();;
        mockHttpSession.setAttribute("cart", cartProductDTOS);
        String json = mockMvc.perform(MockMvcRequestBuilders.delete("/ecommerce/v1/products/3")
                        .session(mockHttpSession)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<CartProductDTO> retrievedCart = objectMapper.readValue(json, new TypeReference<>(){});
        assertFalse(retrievedCart.isEmpty());
        assertEquals(originalCartSize, retrievedCart.size());
    }


    @ParameterizedTest
    @MethodSource("idQuantityArgumentProvider")
    void modifyProductInCart_WithInvalidProduct(Long id, Integer quantity) throws Exception {
        ProductDTO productDTO = ProductDTO.builder().id(id).quantity(quantity).build();
        mockMvc.perform(MockMvcRequestBuilders.put("/ecommerce/v1/products/cart/products/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }

    @Test
    void modifyProductInCart_WithEmptyCart() throws Exception {
        ProductDTO productDTO = ProductDTO.builder().id(1L).quantity(2).build();
        Product product = Product.builder().id(1L).available(5).price(BigDecimal.TEN).build();
        when(productService.getProductIfValidToAddInCart(anyLong(), anyInt())).thenReturn(product);
        mockMvc.perform(MockMvcRequestBuilders.put("/ecommerce/v1/products/cart/products/1")
                        .session(mockHttpSession)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk());
        List<CartProductDTO> cartProductDTOS = (List<CartProductDTO>) mockHttpSession.getAttribute("cart");
        assertNotNull(cartProductDTOS);
        assertFalse(cartProductDTOS.isEmpty());
        verify(productService, times(1)).getProductIfValidToAddInCart(anyLong(), anyInt());
    }

    @Test
    void modifyProductInCart_WithProductNotInCart() throws Exception {
        CartProductDTO cartProductDTO1 = CartProductDTO.builder().id(1L).build();
        CartProductDTO cartProductDTO2 = CartProductDTO.builder().id(2L).build();
        List<CartProductDTO> cartProductDTOS = new ArrayList<>();
        cartProductDTOS.add(cartProductDTO1);
        cartProductDTOS.add(cartProductDTO2);
        int originalSize = cartProductDTOS.size();
        mockHttpSession.setAttribute("cart", cartProductDTOS);
        ProductDTO productDTO = ProductDTO.builder().id(3L).quantity(2).build();
        Product product = Product.builder().id(3L).available(5).price(BigDecimal.TEN).build();
        when(productService.getProductIfValidToAddInCart(anyLong(), anyInt())).thenReturn(product);
        mockMvc.perform(MockMvcRequestBuilders.put("/ecommerce/v1/products/cart/products/3")
                        .session(mockHttpSession)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk());
        List<CartProductDTO> fetchedCartProductDTOS = (List<CartProductDTO>) mockHttpSession.getAttribute("cart");
        assertNotNull(fetchedCartProductDTOS);
        assertEquals(fetchedCartProductDTOS.size(), originalSize + 1);
        verify(productService, times(1)).getProductIfValidToAddInCart(anyLong(), anyInt());
    }

    @Test
    void modifyProductInCart_WithProductIsInCart() throws Exception {
        CartProductDTO cartProductDTO1 = CartProductDTO.builder().id(1L).build();
        CartProductDTO cartProductDTO2 = CartProductDTO.builder().id(2L).build();
        List<CartProductDTO> cartProductDTOS = new ArrayList<>();
        cartProductDTOS.add(cartProductDTO1);
        cartProductDTOS.add(cartProductDTO2);
        int originalSize = cartProductDTOS.size();
        mockHttpSession.setAttribute("cart", cartProductDTOS);
        ProductDTO productDTO = ProductDTO.builder().id(1L).quantity(2).build();
        Product product = Product.builder().id(1L).available(5).price(BigDecimal.TEN).build();
        when(productService.getProductIfValidToAddInCart(anyLong(), anyInt())).thenReturn(product);
        mockMvc.perform(MockMvcRequestBuilders.put("/ecommerce/v1/products/cart/products/1")
                        .session(mockHttpSession)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk());
        List<CartProductDTO> fetchedCartProductDTOS = (List<CartProductDTO>) mockHttpSession.getAttribute("cart");
        assertNotNull(fetchedCartProductDTOS);
        assertEquals(fetchedCartProductDTOS.size(), originalSize);
        verify(productService, times(1)).getProductIfValidToAddInCart(anyLong(), anyInt());
    }

    @Test
    void checkOutProductsInTheCart_WhenCartIsEmpty() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/ecommerce/v1/products/cart/checkout")
                        .session(mockHttpSession)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
        verifyNoInteractions(productService);
    }

    @Test
    void checkOutProductsInTheCart_WhenCartIsNotEmpty() throws Exception {
        CartProductDTO cartProductDTO1 = CartProductDTO.builder().build();
        CartProductDTO cartProductDTO2 = CartProductDTO.builder().build();
        List<CartProductDTO> cartProductDTOS = Arrays.asList(cartProductDTO1, cartProductDTO2);
        Order order = Order.builder().build();
        mockHttpSession.setAttribute("cart", cartProductDTOS);
        when(productService.checkOutProductsInTheCart(any())).thenReturn(order);
        mockMvc.perform(MockMvcRequestBuilders.get("/ecommerce/v1/products/cart/checkout")
                        .session(mockHttpSession)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
        verify(productService, times(1)).checkOutProductsInTheCart(any());
    }

    static Stream<Arguments> idQuantityArgumentProvider() {
        return Stream.of(
                Arguments.of(null, 5),
                Arguments.of(1L, null),
                Arguments.of(1L, 0),
                Arguments.of(1L, -1),
                Arguments.of(null, null)
        );
    }
}