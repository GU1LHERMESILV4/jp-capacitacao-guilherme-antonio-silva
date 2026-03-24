package br.com.indra.guilherme_antonio_silva.controller;

import br.com.indra.guilherme_antonio_silva.service.ReviewService;
import br.com.indra.guilherme_antonio_silva.dto.ReviewCreateRequestDTO;
import br.com.indra.guilherme_antonio_silva.dto.ReviewResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Tag(name = "Reviews", description = "Endpoints para avaliações de produtos")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @Operation(summary = "Criar uma avaliação para um produto")
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestHeader("X-User-Id") String userId,
                                                          @RequestBody ReviewCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(userId, dto));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Listar avaliações de um produto")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }
}

