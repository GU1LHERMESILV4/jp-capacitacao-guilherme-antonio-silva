package br.com.indra.guilherme_antonio_silva.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "historico_preco")
public class HistoricoPreco {

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, nullable = false)
    private String id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "produtos_id")
    private Produtos produtos;
    @Column(name = "preco_antigo")
    private BigDecimal precoAntigo;
    @Column(name = "preco_novo")
    private BigDecimal precoNovo;
    @Column(name = "data_alteracao", updatable = false)
    @CreationTimestamp
    private LocalDateTime dataAlteracao;

}
