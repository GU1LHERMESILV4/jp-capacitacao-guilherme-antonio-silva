package br.com.indra.guilherme_antonio_silva.service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDTO {

    private Long id;
    private String userId;
    private boolean ativo;
    private Integer totalItens;
    private BigDecimal totalValor;
    private List<CartItemResponseDTO> itens;
}

