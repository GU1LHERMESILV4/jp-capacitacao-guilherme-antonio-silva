package br.com.indra.guilherme_antonio_silva.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> itens = new ArrayList<>();

    @Column(name = "total_itens", nullable = false)
    private Integer totalItens = 0;

    @Column(name = "total_valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalValor = BigDecimal.ZERO;
}

