package br.com.indra.guilherme_antonio_silva.service;

import br.com.indra.guilherme_antonio_silva.model.*;
import br.com.indra.guilherme_antonio_silva.repository.CouponUsageRepository;
import br.com.indra.guilherme_antonio_silva.repository.PromotionRepository;
import br.com.indra.guilherme_antonio_silva.repository.CartRepository;
import br.com.indra.guilherme_antonio_silva.service.dto.CouponApplyRequestDTO;
import br.com.indra.guilherme_antonio_silva.service.dto.PromotionRequestDTO;
import br.com.indra.guilherme_antonio_silva.service.dto.CartResponseDTO;
import br.com.indra.guilherme_antonio_silva.service.dto.CartItemResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CartRepository cartRepository;

    public PromotionService(PromotionRepository promotionRepository,
                            CouponUsageRepository couponUsageRepository,
                            CartRepository cartRepository) {
        this.promotionRepository = promotionRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public Promotion createPromotion(PromotionRequestDTO dto) {
        Promotion promotion = Promotion.builder()
                .code(dto.getCode())
                .type(dto.getType())
                .value(dto.getValue())
                .validFrom(dto.getValidFrom())
                .validTo(dto.getValidTo())
                .usageLimit(dto.getUsageLimit())
                .applicableTo(dto.getApplicableTo())
                .build();
        return promotionRepository.save(promotion);
    }

    @Transactional
    public CartResponseDTO applyCoupon(String userId, CouponApplyRequestDTO dto) {
        Promotion promotion = promotionRepository.findByCode(dto.getCode())
                .orElseThrow(() -> new EntityNotFoundException("Cupom não encontrado: " + dto.getCode()));

        OffsetDateTime now = OffsetDateTime.now();
        if (promotion.getValidFrom() != null && now.isBefore(promotion.getValidFrom())) {
            throw new IllegalStateException("Cupom ainda não está válido");
        }
        if (promotion.getValidTo() != null && now.isAfter(promotion.getValidTo())) {
            throw new IllegalStateException("Cupom expirado");
        }
        if (promotion.getUsageLimit() != null && promotion.getUsedCount() >= promotion.getUsageLimit()) {
            throw new IllegalStateException("Limite de uso do cupom atingido");
        }

        // Verificar se usuário já usou este cupom
        couponUsageRepository.findByPromotionAndUserId(promotion, userId)
                .ifPresent(u -> { throw new IllegalStateException("Cupom já utilizado por este usuário"); });

        Cart cart = cartRepository.findByUserIdAndAtivoTrue(userId)
                .orElseThrow(() -> new EntityNotFoundException("Carrinho ativo não encontrado para o usuário: " + userId));

        if (cart.getItens().isEmpty()) {
            throw new IllegalStateException("Carrinho vazio. Não é possível aplicar cupom.");
        }

        // Validar relação com produtos do carrinho
        if (!isApplicableToCart(promotion, cart)) {
            throw new IllegalStateException("Cupom não é aplicável aos itens do carrinho");
        }

        BigDecimal discount = calculateDiscount(promotion, cart.getTotalValor());

        // Aqui usamos campo totalValor do carrinho como base, sem persistir alterações estruturais de carrinho
        BigDecimal newTotal = cart.getTotalValor().subtract(discount);

        // Atualizar métricas de uso da promoção
        promotion.setUsedCount(promotion.getUsedCount() + 1);
        promotionRepository.save(promotion);

        CouponUsage usage = CouponUsage.builder()
                .promotion(promotion)
                .userId(userId)
                .usedAt(OffsetDateTime.now())
                .build();
        couponUsageRepository.save(usage);

        // Montar resposta do carrinho com desconto aplicado somente na visualização
        java.util.List<CartItemResponseDTO> itens = cart.getItens().stream()
                .map(item -> CartItemResponseDTO.builder()
                        .id(item.getId())
                        .produtoId(item.getProduto().getId())
                        .produtoNome(item.getProduto().getNome())
                        .quantidade(item.getQuantidade())
                        .precoUnitarioSnapshot(item.getPrecoUnitarioSnapshot())
                        .totalLinha(item.getTotalLinha())
                        .build())
                .toList();

        return CartResponseDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .ativo(cart.isAtivo())
                .totalItens(cart.getTotalItens())
                .totalValor(newTotal)
                .itens(itens)
                .build();
    }

    private boolean isApplicableToCart(Promotion promotion, Cart cart) {
        String applicableTo = promotion.getApplicableTo();
        if (applicableTo == null || applicableTo.isBlank() || "ALL".equalsIgnoreCase(applicableTo)) {
            return true;
        }

        if (applicableTo.startsWith("CATEGORY:")) {
            Long categoryId = Long.valueOf(applicableTo.substring("CATEGORY:".length()));
            return cart.getItens().stream()
                    .anyMatch(i -> i.getProduto().getCategoria().getId().equals(categoryId));
        }

        if (applicableTo.startsWith("PRODUCT:")) {
            Long productId = Long.valueOf(applicableTo.substring("PRODUCT:".length()));
            return cart.getItens().stream()
                    .anyMatch(i -> i.getProduto().getId().equals(productId));
        }

        return false;
    }

    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal baseTotal) {
        if (promotion.getType() == PromotionType.PERCENTAGE) {
            BigDecimal percent = promotion.getValue().divide(BigDecimal.valueOf(100));
            return baseTotal.multiply(percent);
        }
        if (promotion.getType() == PromotionType.FIXED) {
            return promotion.getValue().min(baseTotal);
        }
        return BigDecimal.ZERO;
    }
}

