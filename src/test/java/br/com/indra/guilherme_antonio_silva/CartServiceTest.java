package br.com.indra.guilherme_antonio_silva;

import br.com.indra.guilherme_antonio_silva.model.Cart;
import br.com.indra.guilherme_antonio_silva.model.CartItem;
import br.com.indra.guilherme_antonio_silva.model.Produtos;
import br.com.indra.guilherme_antonio_silva.repository.CartItemRepository;
import br.com.indra.guilherme_antonio_silva.repository.CartRepository;
import br.com.indra.guilherme_antonio_silva.repository.ProdutosRepository;
import br.com.indra.guilherme_antonio_silva.service.CartService;
import br.com.indra.guilherme_antonio_silva.dto.CartItemRequestDTO;
import br.com.indra.guilherme_antonio_silva.dto.CartResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceTest {

    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository;
    private ProdutosRepository produtosRepository;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        cartItemRepository = mock(CartItemRepository.class);
        produtosRepository = mock(ProdutosRepository.class);
        cartService = new CartService(cartRepository, cartItemRepository, produtosRepository);
    }

    @Test
    @DisplayName("Deve criar carrinho ativo quando não existir para o usuário")
    void deveCriarCarrinhoAtivoQuandoNaoExistir() {
        String userId = "user-1";

        when(cartRepository.findByUserIdAndAtivoTrue(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        CartResponseDTO response = cartService.getOrCreateActiveCart(userId);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(userId, response.getUserId());
        assertTrue(response.isAtivo());
        assertEquals(0, response.getTotalItens());
        assertEquals(BigDecimal.ZERO, response.getTotalValor());

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Deve adicionar item ao carrinho e recalcular totais com snapshot de preço")
    void deveAdicionarItemERecalcularTotais() {
        String userId = "user-1";

        Cart carrinhoExistente = new Cart();
        carrinhoExistente.setId(1L);
        carrinhoExistente.setUserId(userId);
        carrinhoExistente.setAtivo(true);
        carrinhoExistente.setItens(new ArrayList<>());
        carrinhoExistente.setTotalItens(0);
        carrinhoExistente.setTotalValor(BigDecimal.ZERO);

        when(cartRepository.findByUserIdAndAtivoTrue(userId)).thenReturn(Optional.of(carrinhoExistente));

        Produtos produto = new Produtos();
        produto.setId(10L);
        produto.setNome("Produto A");
        produto.setPreco(new BigDecimal("100.00"));

        when(produtosRepository.findById(10L)).thenReturn(Optional.of(produto));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemRequestDTO request = CartItemRequestDTO.builder()
                .produtoId(10L)
                .quantidade(2)
                .build();

        CartResponseDTO response = cartService.addItem(userId, request);

        assertEquals(1L, response.getId());
        assertEquals(2, response.getTotalItens());
        assertEquals(new BigDecimal("200.00"), response.getTotalValor());
        assertEquals(1, response.getItens().size());
        assertEquals(new BigDecimal("100.00"), response.getItens().get(0).getPrecoUnitarioSnapshot());
    }

    @Test
    @DisplayName("Deve atualizar item do carrinho e recalcular totais")
    void deveAtualizarItemERecalcularTotais() {
        String userId = "user-1";

        Cart carrinho = new Cart();
        carrinho.setId(1L);
        carrinho.setUserId(userId);
        carrinho.setAtivo(true);

        Produtos produto = new Produtos();
        produto.setId(10L);
        produto.setNome("Produto A");

        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(carrinho);
        item.setProduto(produto);
        item.setQuantidade(1);
        item.setPrecoUnitarioSnapshot(new BigDecimal("50.00"));
        item.setTotalLinha(new BigDecimal("50.00"));

        List<CartItem> itens = new ArrayList<>();
        itens.add(item);
        carrinho.setItens(itens);

        when(cartItemRepository.findByIdAndCartUserIdAndCartAtivoTrue(100L, userId))
                .thenReturn(Optional.of(item));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemRequestDTO request = CartItemRequestDTO.builder()
                .quantidade(3)
                .build();

        CartResponseDTO response = cartService.updateItem(userId, 100L, request);

        assertEquals(3, response.getTotalItens());
        assertEquals(new BigDecimal("150.00"), response.getTotalValor());
        assertEquals(3, response.getItens().get(0).getQuantidade());
    }

    @Test
    @DisplayName("Deve remover item do carrinho e recalcular totais")
    void deveRemoverItemERecalcularTotais() {
        String userId = "user-1";

        Cart carrinho = new Cart();
        carrinho.setId(1L);
        carrinho.setUserId(userId);
        carrinho.setAtivo(true);

        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(carrinho);
        item.setQuantidade(2);
        item.setTotalLinha(new BigDecimal("40.00"));

        List<CartItem> itens = new ArrayList<>();
        itens.add(item);
        carrinho.setItens(itens);

        when(cartItemRepository.findByIdAndCartUserIdAndCartAtivoTrue(100L, userId))
                .thenReturn(Optional.of(item));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponseDTO response = cartService.removeItem(userId, 100L);

        assertEquals(0, response.getTotalItens());
        assertEquals(BigDecimal.ZERO, response.getTotalValor());
        assertTrue(response.getItens().isEmpty());

        verify(cartItemRepository).delete(item);
    }
}

