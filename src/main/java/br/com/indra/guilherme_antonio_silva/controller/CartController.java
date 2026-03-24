package br.com.indra.guilherme_antonio_silva.controller;

import br.com.indra.guilherme_antonio_silva.service.CartService;
import br.com.indra.guilherme_antonio_silva.dto.CartItemRequestDTO;
import br.com.indra.guilherme_antonio_silva.dto.CartResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@Tag(name = "Carrinho", description = "Endpoints para gerenciamento do carrinho de compras")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Obter carrinho ativo do usuário")
    public ResponseEntity<CartResponseDTO> getCart(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(cartService.getOrCreateActiveCart(userId));
    }

    @PostMapping("/items")
    @Operation(summary = "Adicionar item ao carrinho")
    public ResponseEntity<CartResponseDTO> addItem(@RequestHeader("X-User-Id") String userId,
                                                   @RequestBody CartItemRequestDTO dto) {
        return ResponseEntity.ok(cartService.addItem(userId, dto));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Atualizar item do carrinho")
    public ResponseEntity<CartResponseDTO> updateItem(@RequestHeader("X-User-Id") String userId,
                                                      @PathVariable Long itemId,
                                                      @RequestBody CartItemRequestDTO dto) {
        return ResponseEntity.ok(cartService.updateItem(userId, itemId, dto));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remover item do carrinho")
    public ResponseEntity<CartResponseDTO> removeItem(@RequestHeader("X-User-Id") String userId,
                                                      @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(userId, itemId));
    }
}

