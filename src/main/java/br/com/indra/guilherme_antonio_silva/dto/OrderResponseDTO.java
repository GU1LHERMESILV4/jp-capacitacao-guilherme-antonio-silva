package br.com.indra.guilherme_antonio_silva.dto;

import br.com.indra.guilherme_antonio_silva.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private String userId;
    private OrderStatus status;
    private Long cartId;
    private Integer totalItens;
    private BigDecimal totalValor;
    private BigDecimal discount;
    private BigDecimal freight;
    private BigDecimal total;
    private String address;
    private OffsetDateTime createdAt;
    private OffsetDateTime paidAt;
    private OffsetDateTime shippedAt;
    private OffsetDateTime deliveredAt;
    private OffsetDateTime cancelledAt;
    private List<OrderItemResponseDTO> itens;
}
