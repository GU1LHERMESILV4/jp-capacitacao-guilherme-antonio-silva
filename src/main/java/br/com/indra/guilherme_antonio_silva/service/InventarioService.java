package br.com.indra.guilherme_antonio_silva.service;

import br.com.indra.guilherme_antonio_silva.model.InventarioTransacao;
import br.com.indra.guilherme_antonio_silva.model.InventarioTipoTransacao;
import br.com.indra.guilherme_antonio_silva.model.Produtos;
import br.com.indra.guilherme_antonio_silva.repository.InventarioTransacaoRepository;
import br.com.indra.guilherme_antonio_silva.repository.ProdutosRepository;
import br.com.indra.guilherme_antonio_silva.dto.InventarioAdjustmentRequestDTO;
import br.com.indra.guilherme_antonio_silva.dto.InventarioStatusResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioService {

    private final ProdutosRepository produtosRepository;
    private final InventarioTransacaoRepository inventoryTransactionRepository;

    public InventarioService(ProdutosRepository produtosRepository,
                             InventarioTransacaoRepository inventoryTransactionRepository) {
        this.produtosRepository = produtosRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    @Transactional
    public InventarioStatusResponseDTO adicionarEstoque(Long productId, InventarioAdjustmentRequestDTO dto) {
        validarQuantidade(dto);

        Produtos produto = obterProduto(productId);

        int novoEstoque = produto.getQuantidadeEstoque() + dto.getQuantidade();
        produto.setQuantidadeEstoque(novoEstoque);

        atualizarFlagEstoqueMinimo(produto);

        produtosRepository.save(produto);

        registrarTransacao(produto, InventarioTipoTransacao.ENTRADA, dto.getQuantidade(), novoEstoque, dto.getDescricao());

        return toStatusDTO(produto);
    }

    @Transactional
    public InventarioStatusResponseDTO removerEstoque(Long productId, InventarioAdjustmentRequestDTO dto) {
        validarQuantidade(dto);

        Produtos produto = obterProduto(productId);

        if (produto.getQuantidadeEstoque() < dto.getQuantidade()) {
            throw new IllegalStateException("Estoque insuficiente para remoção");
        }

        int novoEstoque = produto.getQuantidadeEstoque() - dto.getQuantidade();
        produto.setQuantidadeEstoque(novoEstoque);

        atualizarFlagEstoqueMinimo(produto);

        produtosRepository.save(produto);

        registrarTransacao(produto, InventarioTipoTransacao.SAIDA, dto.getQuantidade(), novoEstoque, dto.getDescricao());

        return toStatusDTO(produto);
    }

    @Transactional(readOnly = true)
    public InventarioStatusResponseDTO consultarEstoque(Long productId) {
        Produtos produto = obterProduto(productId);
        return toStatusDTO(produto);
    }

    @Transactional(readOnly = true)
    public List<InventarioTransacao> listarTransacoes(Long productId) {
        Produtos produto = obterProduto(productId);
        return inventoryTransactionRepository.findByProdutoOrderByDataTransacaoDesc(produto);
    }

    private void validarQuantidade(InventarioAdjustmentRequestDTO dto) {
        if (dto.getQuantidade() == null || dto.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
    }

    private Produtos obterProduto(Long productId) {
        return produtosRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado para o id: " + productId));
    }

    private void registrarTransacao(Produtos produto,
                                    InventarioTipoTransacao tipo,
                                    Integer quantidade,
                                    Integer estoqueResultante,
                                    String descricao) {

        InventarioTransacao transacao = InventarioTransacao.builder()
                .produto(produto)
                .tipo(tipo)
                .quantidade(quantidade)
                .estoqueResultante(estoqueResultante)
                .descricao(descricao)
                .build();

        inventoryTransactionRepository.save(transacao);
    }

    private void atualizarFlagEstoqueMinimo(Produtos produto) {
        boolean abaixoOuIgual = produto.getQuantidadeEstoque() != null
                && produto.getEstoqueMinimo() != null
                && produto.getQuantidadeEstoque() <= produto.getEstoqueMinimo();

        produto.setEstoqueAbaixoMinimo(abaixoOuIgual);
    }

    private InventarioStatusResponseDTO toStatusDTO(Produtos produto) {
        return InventarioStatusResponseDTO.builder()
                .produtoId(produto.getId())
                .quantidadeEstoque(produto.getQuantidadeEstoque())
                .estoqueMinimo(produto.getEstoqueMinimo())
                .estoqueAbaixoMinimo(produto.getEstoqueAbaixoMinimo())
                .build();
    }
}

