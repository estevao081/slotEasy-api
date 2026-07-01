package com.slotes.api.controller;

import com.slotes.api.dto.ProductDTOs.ProductRequest;
import com.slotes.api.dto.ProductDTOs.ProductResponse;
import com.slotes.api.model.Product;
import com.slotes.api.repository.ProductRepository;
import com.slotes.api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "CRUD de produtos")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @Autowired
    ProductRepository productRepository;

    @PostMapping
    @Operation(summary = "Criar produto")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos os produtos")
    public ResponseEntity<List<ProductResponse>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Buscar produto por código (tela de impressão)")
    public ResponseEntity<ProductResponse> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(productService.findByCode(code));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir produto")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/products/import")
    public ResponseEntity<String> importar(@RequestParam("file") MultipartFile file) throws IOException {

        DataFormatter formatter = new DataFormatter();

        List<Product> produtos = new ArrayList<>();
        Set<String> codigosPlanilha = new HashSet<>();
        Set<String> codigosBanco = new HashSet<>(productRepository.findAllCodes());

        int importados = 0;
        int duplicadosPlanilha = 0;
        int duplicadosBanco = 0;
        int linhasVazias = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            // Começa na segunda linha (índice 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    linhasVazias++;
                    continue;
                }

                String codigo = formatter.formatCellValue(row.getCell(0)).trim();
                String nome = formatter.formatCellValue(row.getCell(1)).trim();

                // Ignora linhas vazias
                if (codigo.isBlank() && nome.isBlank()) {
                    linhasVazias++;
                    continue;
                }

                // Código duplicado na planilha
                if (!codigosPlanilha.add(codigo)) {
                    duplicadosPlanilha++;
                    System.out.println("Código duplicado na planilha: " + codigo);
                    continue;
                }

                // Código já existente no banco
                if (codigosBanco.contains(codigo)) {
                    duplicadosBanco++;
                    System.out.println("Código já existe no banco: " + codigo);
                    continue;
                }

                Product produto = new Product();
                produto.setCode(codigo);
                produto.setName(nome);

                produtos.add(produto);

                // Evita que um mesmo código apareça novamente durante esta importação
                codigosBanco.add(codigo);

                // Salva em lotes de 500
                if (produtos.size() == 500) {

                    productRepository.saveAll(produtos);

                    importados += produtos.size();

                    System.out.println(importados + " produtos importados...");

                    produtos.clear();
                }
            }

            // Salva o restante
            if (!produtos.isEmpty()) {
                productRepository.saveAll(produtos);
                importados += produtos.size();
            }
        }

        return ResponseEntity.ok(
                """
                Importação concluída!
    
                Produtos importados: %d
                Duplicados na planilha: %d
                Já existentes no banco: %d
                Linhas vazias: %d
                """
                        .formatted(
                                importados,
                                duplicadosPlanilha,
                                duplicadosBanco,
                                linhasVazias
                        )
        );
    }
}
