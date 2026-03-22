package br.com.indra.guilherme_antonio_silva.repository;

import br.com.indra.guilherme_antonio_silva.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNomeAndCategoriaPaiId(String nome, Long categoriaPaiId);

    Optional<Categoria> findByNomeAndCategoriaPaiIsNull(String nome);
}

