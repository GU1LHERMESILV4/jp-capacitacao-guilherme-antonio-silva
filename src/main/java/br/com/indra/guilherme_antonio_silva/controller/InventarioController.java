package br.com.indra.guilherme_antonio_silva.controller;

import br.com.indra.guilherme_antonio_silva.model.InventarioTransacao;
import br.com.indra.guilherme_antonio_silva.service.InventarioService;
import br.com.indra.guilherme_antonio_silva.dto.InventarioAdjustmentRequestDTO;
import br.com.indra.guilherme_antonio_silva.dto.InventarioStatusResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@Tag(name = "Inventário", description = "Controle de estoque dos produtos")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PostMapping("/{productId}/add")
    @Operation(summary = "Adicionar estoque de um produto")
    public ResponseEntity<InventarioStatusResponseDTO> adicionarEstoque(
            @PathVariable Long productId,
            @RequestBody InventarioAdjustmentRequestDTO dto
    ) {
        return ResponseEntity.ok(inventarioService.adicionarEstoque(productId, dto));
    }

    @PostMapping("/{productId}/remove")
    @Operation(summary = "Remover estoque de um produto (venda/saída)")
    public ResponseEntity<InventarioStatusResponseDTO> removerEstoque(
            @PathVariable Long productId,
            @RequestBody InventarioAdjustmentRequestDTO dto
    ) {
        return ResponseEntity.ok(inventarioService.removerEstoque(productId, dto));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Consultar saldo de estoque de um produto")
    public ResponseEntity<InventarioStatusResponseDTO> consultarEstoque(@PathVariable Long productId) {
        return ResponseEntity.ok(inventarioService.consultarEstoque(productId));
    }

    @GetMapping("/{productId}/transactions")
    @Operation(summary = "Listar transações de inventário de um produto")
    public ResponseEntity<List<InventarioTransacao>> listarTransacoes(@PathVariable Long productId) {
        return ResponseEntity.ok(inventarioService.listarTransacoes(productId));
    }
}

