package com.slotes.api.mapper;

import com.slotes.api.dto.ProductDTOs.ProductRequest;
import com.slotes.api.dto.ProductDTOs.ProductResponse;
import com.slotes.api.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        return Product.builder()
                .code(request.code())
                .name(request.name())
                .build();
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getCode(), product.getName());
    }
}
