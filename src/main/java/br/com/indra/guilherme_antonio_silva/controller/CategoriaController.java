package br.com.indra.guilherme_antonio_silva.controller;

import br.com.indra.guilherme_antonio_silva.service.CategoriaService;
import br.com.indra.guilherme_antonio_silva.dto.CategoriaRequestDTO;
import br.com.indra.guilherme_antonio_silva.dto.CategoriaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar todas as categorias")
    public ResponseEntity<List<CategoriaResponseDTO>> listar() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @PostMapping
    @Operation(summary = "Criar nova categoria")
    public ResponseEntity<CategoriaResponseDTO> criar(@RequestBody CategoriaRequestDTO dto) {
        CategoriaResponseDTO criada = categoriaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria existente")
    public ResponseEntity<CategoriaResponseDTO> atualizar(@PathVariable Long id,
                                                          @RequestBody CategoriaRequestDTO dto) {
        CategoriaResponseDTO atualizada = categoriaService.atualizar(id, dto);
        return ResponseEntity.ok(atualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        categoriaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

