package br.com.indra.guilherme_antonio_silva.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produtos produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario_snapshot", nullable = false, precision = 19, scale = 2)
    private BigDecimal precoUnitarioSnapshot;

    @Column(name = "total_linha", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalLinha;
}

