package br.com.indra.guilherme_antonio_silva.service;

import br.com.indra.guilherme_antonio_silva.model.Order;
import br.com.indra.guilherme_antonio_silva.model.OrderItem;
import br.com.indra.guilherme_antonio_silva.model.Produtos;
import br.com.indra.guilherme_antonio_silva.model.Review;
import br.com.indra.guilherme_antonio_silva.repository.OrderRepository;
import br.com.indra.guilherme_antonio_silva.repository.ReviewRepository;
import br.com.indra.guilherme_antonio_silva.repository.ProdutosRepository;
import br.com.indra.guilherme_antonio_silva.dto.ReviewCreateRequestDTO;
import br.com.indra.guilherme_antonio_silva.dto.ReviewResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProdutosRepository produtosRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         OrderRepository orderRepository,
                         ProdutosRepository produtosRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.produtosRepository = produtosRepository;
    }

    @Transactional
    public ReviewResponseDTO createReview(String userId, ReviewCreateRequestDTO dto) {
        Produtos product = produtosRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado para o id: " + dto.getProductId()));

        // Verificar se usuário já avaliou este produto
        reviewRepository.findByProductIdAndUserId(dto.getProductId(), userId)
                .ifPresent(r -> { throw new IllegalStateException("Usuário já avaliou este produto"); });

        // Verificar se usuário comprou o produto
        boolean hasPurchased = orderRepository.findAll().stream()
                .filter(o -> o.getUserId().equals(userId))
                .map(Order::getItens)
                .flatMap(List::stream)
                .map(OrderItem::getProduto)
                .anyMatch(p -> p.getId().equals(dto.getProductId()));

        if (!hasPurchased) {
            throw new IllegalStateException("Apenas usuários que compraram o produto podem avaliá-lo");
        }

        Review review = Review.builder()
                .product(product)
                .userId(userId)
                .rating(dto.getRating())
                .review_comment(dto.getComment())
                .createdAt(OffsetDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);

        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private ReviewResponseDTO toResponseDTO(Review review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getReview_comment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}

