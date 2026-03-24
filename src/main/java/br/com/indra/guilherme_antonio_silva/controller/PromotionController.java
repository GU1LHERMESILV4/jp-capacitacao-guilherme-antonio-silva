package br.com.indra.guilherme_antonio_silva.controller;

import br.com.indra.guilherme_antonio_silva.model.Promotion;
import br.com.indra.guilherme_antonio_silva.service.PromotionService;
import br.com.indra.guilherme_antonio_silva.dto.CouponApplyRequestDTO;
import br.com.indra.guilherme_antonio_silva.dto.PromotionRequestDTO;
import br.com.indra.guilherme_antonio_silva.dto.CartResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Promoções e Cupons", description = "Endpoints para gerenciamento de promoções e aplicação de cupons")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping("/promotions")
    @Operation(summary = "Criar uma promoção/cupom")
    public ResponseEntity<Promotion> createPromotion(@RequestBody PromotionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.createPromotion(dto));
    }

    @PostMapping("/coupons/apply")
    @Operation(summary = "Aplicar cupom ao carrinho ativo do usuário")
    public ResponseEntity<CartResponseDTO> applyCoupon(@RequestHeader("X-User-Id") String userId,
                                                       @RequestBody CouponApplyRequestDTO dto) {
        return ResponseEntity.ok(promotionService.applyCoupon(userId, dto));
    }
}

