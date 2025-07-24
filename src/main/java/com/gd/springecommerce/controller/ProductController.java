package com.gd.springecommerce.controller;

import com.gd.springecommerce.dto.CartProductDTO;
import com.gd.springecommerce.dto.ProductDTO;
import com.gd.springecommerce.mapper.ProductMapper;
import com.gd.springecommerce.model.EcommerceResponse;
import com.gd.springecommerce.model.Order;
import com.gd.springecommerce.model.Product;
import com.gd.springecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequestMapping("/ecommerce/v1/products")
@Tag(name = "Product Manager", description = "API for managing the products and user cart information")
public class ProductController {

    ProductService productService;

    @Operation(summary = "Get all the products from the inventory", description = "Fetches all the products from the inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Retrieval"),
            @ApiResponse(responseCode = "500", description = "Internal server error")}
    )
    @GetMapping(value = "/all-products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(summary = "Add items to the cart", description = "Takes id and quantity of the product, checks if the product is available in required quantity and adds it to the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added to the cart"),
            @ApiResponse(responseCode = "400", description = "Error in the data to be inserted"), @ApiResponse(responseCode = "403", description = "Product is not found in the inventory"),
            @ApiResponse(responseCode = "409", description = "product is out of stock")}
    )
    @PostMapping(path = "/cart", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EcommerceResponse> addItemsToCart(@Valid @RequestBody ProductDTO productDTO, BindingResult result, HttpServletRequest request) {
        if (!result.hasErrors()) {
            Product product = productService.getProductIfValidToAddInCart(productDTO.getId(), productDTO.getQuantity());
            HttpSession session = request.getSession();
            List<CartProductDTO> products = (List<CartProductDTO>) session.getAttribute("cart");
            if (products == null) {
                products = new ArrayList<>();
            }
            CartProductDTO cartProductDTO = ProductMapper.INSTANCE.toCartProductDTO(product, productDTO);
            products.add(cartProductDTO);
            session.setAttribute("cart", products);
            return new ResponseEntity<>(new EcommerceResponse<>(cartProductDTO), HttpStatus.OK);
        }
        Set<String> errorMessages = result.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toSet());
        return new ResponseEntity<>(new EcommerceResponse<>(errorMessages), HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "Display user cart", description = "Display the product name along with the id and quantity added in the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully fetched the products in the cart"),
            @ApiResponse(responseCode = "500", description = "Internal server error")}
    )
    @GetMapping(path = "/cart/products")
    public ResponseEntity<List<CartProductDTO>> displayCart(HttpSession session) {
        List<CartProductDTO> products = (List<CartProductDTO>) session.getAttribute("cart");
        if (products == null) {
            products = Collections.emptyList();
        }
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Delete a product in a user cart", description = "Takes the id from the user and deletes the product in the cart and fetches the remaining items in the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched the remaining products"), @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "403", description = "Product is not found in the inventory")}
    )
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<List<CartProductDTO>> removeProductFromCart(@PathVariable("id") Long id, HttpSession session) {
        List<CartProductDTO> products = (List<CartProductDTO>) session.getAttribute("cart");
        if (products == null) {
            products = Collections.emptyList();
        } else {
            products.removeIf(cartProductDTO -> cartProductDTO.getId().equals(id));
        }
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Updates quantity of the product in the cart", description = "Takes the id and updated quantity of the product and updates the quantity of the product in the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Updated"), @ApiResponse(responseCode = "400", description = "Error in the data to be inserted"),
            @ApiResponse(responseCode = "204", description = "Cart is empty")}
    )
    @PutMapping(path = "/cart/products/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EcommerceResponse> modifyProductInCart(@Valid @RequestBody ProductDTO productDTO, BindingResult result, @PathVariable("id") Long id, HttpSession session) {
        if (!result.hasErrors()) {
            Product product = productService.getProductIfValidToAddInCart(productDTO.getId(), productDTO.getQuantity());
            List<CartProductDTO> products = (List<CartProductDTO>) session.getAttribute("cart");
            CartProductDTO cartProductDTO = ProductMapper.INSTANCE.toCartProductDTO(product, productDTO);
            if (products == null) {
                products = new ArrayList<>();
                products.add(cartProductDTO);
                session.setAttribute("cart", products);
            } else {
                boolean isProductNotInCart = products.stream().noneMatch(cartProduct -> cartProduct.getId().equals(id));
                if (isProductNotInCart) {
                    products.add(cartProductDTO);
                } else {
                    products.replaceAll(cartProduct -> cartProduct.getId().equals(id) ? cartProductDTO : cartProduct);
                }
            }
            return ResponseEntity.ok(new EcommerceResponse<>(cartProductDTO));
        }
        Set<String> errorMessages = result.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toSet());
        return new ResponseEntity<>(new EcommerceResponse<>(errorMessages), HttpStatus.BAD_REQUEST);

    }

    @Operation(summary = "Checkout products in the cart", description = "Checks out product in the cart and generates the order details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully Placed the order"),
            @ApiResponse(responseCode = "204", description = "Cart is empty")}
    )
    @GetMapping(path = "/cart/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Order> checkOutProductsInTheCart(HttpSession session) {
        List<CartProductDTO> products = (List<CartProductDTO>) session.getAttribute("cart");
        if (products == null) {
            return ResponseEntity.noContent().build();
        } else {
            Order order = productService.checkOutProductsInTheCart(products);
            return ResponseEntity.ok(order);
        }
    }
}
