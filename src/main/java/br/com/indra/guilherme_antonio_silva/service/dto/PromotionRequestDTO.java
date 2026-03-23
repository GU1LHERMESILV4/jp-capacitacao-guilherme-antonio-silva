package br.com.indra.guilherme_antonio_silva.service.dto;

import br.com.indra.guilherme_antonio_silva.model.PromotionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PromotionRequestDTO {

    private String code;
    private PromotionType type;
    private BigDecimal value;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private Integer usageLimit;
    private String applicableTo;
}

