package br.com.indra.guilherme_antonio_silva.service;

import br.com.indra.guilherme_antonio_silva.model.HistoricoPreco;
import br.com.indra.guilherme_antonio_silva.repository.HistoricoPrecoRepository;
import br.com.indra.guilherme_antonio_silva.dto.HistoricoProdutoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final HistoricoPrecoRepository historicoPrecoRepository;

    public List<HistoricoProdutoDTO> getHistoricoByProdutoId(Long produtoId) {
        Set<HistoricoPreco> historicoPrecos = historicoPrecoRepository.findByProdutosId(produtoId);

        return historicoPrecos.stream()
                .map(this::toDto)
                .toList();
    }

    private HistoricoProdutoDTO toDto(HistoricoPreco historicoPreco) {
        return HistoricoProdutoDTO.builder()
                .id(historicoPreco.getId())
                .produto(historicoPreco.getProdutos().getNome())
                .precoAntigo(historicoPreco.getPrecoAntigo())
                .precoNovo(historicoPreco.getPrecoNovo())
                .dataRegistro(historicoPreco.getDataAlteracao())
                .build();
    }
}