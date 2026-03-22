package br.com.indra.guilherme_antonio_silva.service;

import br.com.indra.guilherme_antonio_silva.model.HistoricoPreco;
import br.com.indra.guilherme_antonio_silva.model.Produtos;
import br.com.indra.guilherme_antonio_silva.repository.HistoricoPrecoRepository;
import br.com.indra.guilherme_antonio_silva.repository.ProdutosRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutosService {

    private final ProdutosRepository produtosRepository;
    private final HistoricoPrecoRepository historicoPrecoRepository;

    public List<Produtos> listarProdutos() {
        return produtosRepository.findAll();
    }

    public Produtos criarProduto(Produtos produto) {
        return produtosRepository.save(produto);
    }

    public Produtos atualizarProdutoPorId(Long id, Produtos produtoAtualizado) {
        Produtos produtoExistente = obterProdutoPorId(id);
        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setDescricao(produtoAtualizado.getDescricao());
        produtoExistente.setPreco(produtoAtualizado.getPreco());
        produtoExistente.setCodigoBarras(produtoAtualizado.getCodigoBarras());
        return produtosRepository.save(produtoExistente);
    }

    public void removerProdutoPorId(Long id) {
        produtosRepository.deleteById(id);
    }

    public Produtos buscarProdutoPorId(Long id) {
        return obterProdutoPorId(id);
    }

    public Produtos atualizarPrecoProduto(Long id, BigDecimal preco) {
        final var produto = obterProdutoPorId(id);

        final var precoAntigo = produto.getPreco();
        produto.setPreco(preco);

        final var historico = new HistoricoPreco();
        historico.setPrecoAntigo(precoAntigo);
        historico.setProdutos(produto);
        historico.setPrecoNovo(preco);

        historicoPrecoRepository.save(historico);
        return produtosRepository.saveAndFlush(produto);
    }

    private Produtos obterProdutoPorId(Long id) {
        return produtosRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado para o id: " + id));
    }
}
