package br.com.indra.guilherme_antonio_silva.dto;

import lombok.Data;

@Data
public class ReviewCreateRequestDTO {

    private Long productId;
    private Integer rating;
    private String comment;
}

