package br.com.indra.guilherme_antonio_silva.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoricoProdutoDTO {

    private String id;
    private String produto;
    private BigDecimal precoAntigo;
    private BigDecimal precoNovo;
    private LocalDateTime dataRegistro;

}
