package br.com.indra.guilherme_antonio_silva;

import br.com.indra.guilherme_antonio_silva.model.InventarioTransacao;
import br.com.indra.guilherme_antonio_silva.model.InventarioTipoTransacao;
import br.com.indra.guilherme_antonio_silva.model.Produtos;
import br.com.indra.guilherme_antonio_silva.repository.InventarioTransacaoRepository;
import br.com.indra.guilherme_antonio_silva.repository.ProdutosRepository;
import br.com.indra.guilherme_antonio_silva.service.InventarioService;
import br.com.indra.guilherme_antonio_silva.service.dto.InventarioAdjustmentRequestDTO;
import br.com.indra.guilherme_antonio_silva.service.dto.InventarioStatusResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventarioServiceTest {

    private ProdutosRepository produtosRepository;
    private InventarioTransacaoRepository inventarioTransacaoRepository;
    private InventarioService inventarioService;

    @BeforeEach
    void setUp() {
        produtosRepository = mock(ProdutosRepository.class);
        inventarioTransacaoRepository = mock(InventarioTransacaoRepository.class);
        inventarioService = new InventarioService(produtosRepository, inventarioTransacaoRepository);
    }

    @Test
    @DisplayName("Deve adicionar estoque e registrar transação de entrada")
    void deveAdicionarEstoque() {
        Produtos produto = new Produtos();
        produto.setId(1L);
        produto.setQuantidadeEstoque(10);
        produto.setEstoqueMinimo(5);
        produto.setEstoqueAbaixoMinimo(false);

        when(produtosRepository.findById(1L)).thenReturn(Optional.of(produto));

        InventarioAdjustmentRequestDTO request = InventarioAdjustmentRequestDTO.builder()
                .quantidade(5)
                .descricao("Reposição")
                .build();

        InventarioStatusResponseDTO response = inventarioService.adicionarEstoque(1L, request);

        assertEquals(15, response.getQuantidadeEstoque());
        assertFalse(response.getEstoqueAbaixoMinimo());

        ArgumentCaptor<InventarioTransacao> captor = ArgumentCaptor.forClass(InventarioTransacao.class);
        verify(inventarioTransacaoRepository).save(captor.capture());
        assertEquals(InventarioTipoTransacao.ENTRADA, captor.getValue().getTipo());
        assertEquals(5, captor.getValue().getQuantidade());
        assertEquals(15, captor.getValue().getEstoqueResultante());
    }

    @Test
    @DisplayName("Não deve permitir remoção de estoque superior ao disponível")
    void naoDevePermitirRemocaoComEstoqueInsuficiente() {
        Produtos produto = new Produtos();
        produto.setId(1L);
        produto.setQuantidadeEstoque(3);
        produto.setEstoqueMinimo(1);

        when(produtosRepository.findById(1L)).thenReturn(Optional.of(produto));

        InventarioAdjustmentRequestDTO request = InventarioAdjustmentRequestDTO.builder()
                .quantidade(5)
                .descricao("Venda")
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> inventarioService.removerEstoque(1L, request));

        assertTrue(ex.getMessage().toLowerCase().contains("estoque insuficiente"));
        verify(inventarioTransacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar flag de estoque mínimo quando atingir o limite")
    void deveAtualizarFlagEstoqueMinimo() {
        Produtos produto = new Produtos();
        produto.setId(1L);
        produto.setQuantidadeEstoque(10);
        produto.setEstoqueMinimo(5);
        produto.setEstoqueAbaixoMinimo(false);

        when(produtosRepository.findById(1L)).thenReturn(Optional.of(produto));

        InventarioAdjustmentRequestDTO request = InventarioAdjustmentRequestDTO.builder()
                .quantidade(5)
                .descricao("Venda grande")
                .build();

        InventarioStatusResponseDTO response = inventarioService.removerEstoque(1L, request);

        assertEquals(5, response.getQuantidadeEstoque());
        assertTrue(response.getEstoqueAbaixoMinimo());
    }
}

