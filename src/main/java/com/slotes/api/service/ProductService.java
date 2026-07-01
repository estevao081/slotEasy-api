package com.slotes.api.service;

import com.slotes.api.dto.ProductDTOs.ProductRequest;
import com.slotes.api.dto.ProductDTOs.ProductResponse;
import com.slotes.api.exception.DuplicateResourceException;
import com.slotes.api.exception.ResourceNotFoundException;
import com.slotes.api.mapper.ProductMapper;
import com.slotes.api.model.Product;
import com.slotes.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Código já cadastrado: " + request.code());
        }
        Product product = productMapper.toEntity(request);
        product.setName(product.getName().toUpperCase());
        return productMapper.toResponse(productRepository.save(product));
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.toResponse(product);
    }

    public ProductResponse findByCode(String code) {
        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.toResponse(product);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (productRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new DuplicateResourceException("Código já cadastrado: " + request.code());
        }

        product.setCode(request.code());
        product.setName(request.name());
        return productMapper.toResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }
}
