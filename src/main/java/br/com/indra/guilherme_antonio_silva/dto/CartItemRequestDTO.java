package br.com.indra.guilherme_antonio_silva.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemRequestDTO {

    private Long produtoId;
    private Integer quantidade;
    private BigDecimal precoUnitario;
}

