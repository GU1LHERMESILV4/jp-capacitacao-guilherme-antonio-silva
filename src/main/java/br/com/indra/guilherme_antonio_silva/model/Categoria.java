package br.com.indra.guilherme_antonio_silva.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "categorias",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_categoria_nome_pai",
                        columnNames = {"nome", "categoria_pai_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_pai_id")
    private Categoria categoriaPai;

    @OneToMany(mappedBy = "categoriaPai")
    @Builder.Default
    private Set<Categoria> subcategorias = new HashSet<>();

    @OneToMany(mappedBy = "categoria")
    @Builder.Default
    private Set<Produtos> produtos = new HashSet<>();
}

