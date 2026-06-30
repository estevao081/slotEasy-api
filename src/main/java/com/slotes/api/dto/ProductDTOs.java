package com.slotes.api.dto;

import jakarta.validation.constraints.NotBlank;

public class ProductDTOs {

    public record ProductRequest(
            @NotBlank(message = "Código é obrigatório")
            String code,

            @NotBlank(message = "Nome é obrigatório")
            String name
    ) {}

    public record ProductResponse(
            Long id,
            String code,
            String name
    ) {}
}
