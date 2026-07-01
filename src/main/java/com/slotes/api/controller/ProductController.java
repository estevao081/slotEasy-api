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
import java.util.List;

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

    @PostMapping("/import")
    public ResponseEntity<String> importar(@RequestParam("file") MultipartFile file) throws IOException {

        DataFormatter formatter = new DataFormatter();
        List<Product> produtos = new ArrayList<>();
        int totalImportados = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            // Começa da linha 1 (segunda linha da planilha)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                String codigo = formatter.formatCellValue(row.getCell(0)).trim();
                String nome = formatter.formatCellValue(row.getCell(1)).trim();

                // Ignora linhas vazias
                if (codigo.isBlank() && nome.isBlank()) {
                    continue;
                }

                Product produto = new Product();
                produto.setCode(codigo);
                produto.setName(nome);

                produtos.add(produto);

                // Salva em lotes de 500
                if (produtos.size() == 500) {
                    productRepository.saveAll(produtos);
                    totalImportados += produtos.size();
                    produtos.clear();

                    System.out.println(totalImportados + " produtos importados...");
                }
            }

            // Salva o restante
            if (!produtos.isEmpty()) {
                productRepository.saveAll(produtos);
                totalImportados += produtos.size();
            }
        }

        return ResponseEntity.ok(
                "Importação concluída. Total de produtos importados: " + totalImportados
        );
    }
}
