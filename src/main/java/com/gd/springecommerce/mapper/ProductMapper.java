package com.gd.springecommerce.mapper;

import com.gd.springecommerce.dto.CartProductDTO;
import com.gd.springecommerce.dto.ProductDTO;
import com.gd.springecommerce.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
    @Mapping(source = "product.id", target = "id")
    @Mapping(source = "product.title", target = "title")
    @Mapping(source = "productDTO.quantity", target = "quantity")
    @Mapping(target = "subTotal", expression = "java(product.getPrice().multiply(java.math.BigDecimal.valueOf(productDTO.getQuantity())))")
    CartProductDTO toCartProductDTO(Product product, ProductDTO productDTO);
    Product cartProductDTOToProduct(CartProductDTO cartProductDTO);
}
