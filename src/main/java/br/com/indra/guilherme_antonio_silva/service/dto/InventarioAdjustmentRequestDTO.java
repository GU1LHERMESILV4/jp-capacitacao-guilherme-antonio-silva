package br.com.indra.guilherme_antonio_silva.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioAdjustmentRequestDTO {

    private Integer quantidade;
    private String descricao;
}

