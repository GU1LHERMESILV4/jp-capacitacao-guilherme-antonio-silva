package br.com.indra.guilherme_antonio_silva.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {

    private Long id;
    private Long productId;
    private String userId;
    private Integer rating;
    private String comment;
    private OffsetDateTime createdAt;
}

