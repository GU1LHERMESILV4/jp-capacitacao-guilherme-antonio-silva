package br.com.indra.guilherme_antonio_silva.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioTransacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produtos produto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private InventarioTipoTransacao tipo;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "estoque_resultante", nullable = false)
    private Integer estoqueResultante;

    @Column(name = "descricao")
    private String descricao;

    @CreationTimestamp
    @Column(name = "data_transacao", updatable = false)
    private LocalDateTime dataTransacao;
}

