package br.com.indra.guilherme_antonio_silva.repository;

import br.com.indra.guilherme_antonio_silva.model.InventarioTransacao;
import br.com.indra.guilherme_antonio_silva.model.Produtos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventarioTransacaoRepository extends JpaRepository<InventarioTransacao, Long> {

    List<InventarioTransacao> findByProdutoOrderByDataTransacaoDesc(Produtos produto);
}

