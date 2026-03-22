package br.com.indra.guilherme_antonio_silva.repository;

import br.com.indra.guilherme_antonio_silva.model.Produtos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutosRepository extends JpaRepository<Produtos, Long> {
}