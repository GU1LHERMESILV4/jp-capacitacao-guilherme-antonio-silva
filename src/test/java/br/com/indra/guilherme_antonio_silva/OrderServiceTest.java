package br.com.indra.guilherme_antonio_silva;

import br.com.indra.guilherme_antonio_silva.model.*;
import br.com.indra.guilherme_antonio_silva.repository.CartRepository;
import br.com.indra.guilherme_antonio_silva.repository.OrderRepository;
import br.com.indra.guilherme_antonio_silva.service.InventarioService;
import br.com.indra.guilherme_antonio_silva.service.OrderService;
import br.com.indra.guilherme_antonio_silva.service.dto.OrderCreateRequestDTO;
import br.com.indra.guilherme_antonio_silva.service.dto.OrderResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrderFromCart_deveCriarPedidoAPartirDoCarrinho() {
        String userId = "user-1";

        Produtos produto = new Produtos();
        produto.setId(1L);
        produto.setNome("Produto Teste");
        produto.setPreco(BigDecimal.valueOf(10));

        CartItem item = CartItem.builder()
                .produto(produto)
                .quantidade(2)
                .precoUnitarioSnapshot(BigDecimal.valueOf(10))
                .totalLinha(BigDecimal.valueOf(20))
                .build();

        Cart cart = Cart.builder()
                .id(1L)
                .userId(userId)
                .ativo(true)
                .itens(List.of(item))
                .totalItens(2)
                .totalValor(BigDecimal.valueOf(20))
                .build();

        item.setCart(cart);

        when(cartRepository.findByUserIdAndAtivoTrue(userId)).thenReturn(Optional.of(cart));
        when(orderRepository.save(ArgumentMatchers.any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(100L);
            o.getItens().forEach(oi -> oi.setId(200L));
            return o;
        });

        OrderResponseDTO response = orderService.createOrderFromCart(userId, new OrderCreateRequestDTO());

        assertNotNull(response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(OrderStatus.CREATED, response.getStatus());
        assertEquals(2, response.getTotalItens());
        assertEquals(BigDecimal.valueOf(20), response.getTotalValor());
        assertEquals(1, response.getItens().size());

        verify(inventarioService, times(1)).removerEstoque(eq(1L), any());
        assertFalse(cart.isAtivo());
    }

    @Test
    void createOrderFromCart_deveLancarExcecaoSeCarrinhoNaoExiste() {
        String userId = "user-1";
        when(cartRepository.findByUserIdAndAtivoTrue(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> orderService.createOrderFromCart(userId, new OrderCreateRequestDTO()));
    }

    @Test
    void createOrderFromCart_deveLancarExcecaoSeCarrinhoVazio() {
        String userId = "user-1";
        Cart cart = Cart.builder()
                .id(1L)
                .userId(userId)
                .ativo(true)
                .itens(List.of())
                .totalItens(0)
                .totalValor(BigDecimal.ZERO)
                .build();

        when(cartRepository.findByUserIdAndAtivoTrue(userId)).thenReturn(Optional.of(cart));

        assertThrows(IllegalStateException.class,
                () -> orderService.createOrderFromCart(userId, new OrderCreateRequestDTO()));
    }

    @Test
    void cancelOrder_deveCancelarPedidoQuandoStatusCreatedOuPaidEReporEstoque() {
        String userId = "user-1";

        Produtos produto = new Produtos();
        produto.setId(1L);
        produto.setNome("Produto Teste");

        OrderItem orderItem = OrderItem.builder()
                .produto(produto)
                .quantidade(2)
                .precoUnitarioSnapshot(BigDecimal.valueOf(10))
                .totalLinha(BigDecimal.valueOf(20))
                .build();

        Order order = Order.builder()
                .id(100L)
                .userId(userId)
                .status(OrderStatus.CREATED)
                .totalItens(2)
                .totalValor(BigDecimal.valueOf(20))
                .itens(List.of(orderItem))
                .build();

        orderItem.setOrder(order);

        when(orderRepository.findByIdAndUserId(100L, userId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = orderService.cancelOrder(100L, userId);

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        verify(inventarioService, times(1)).adicionarEstoque(eq(1L), any());
    }

    @Test
    void cancelOrder_deveLancarExcecaoQuandoStatusNaoPermitido() {
        String userId = "user-1";

        Order order = Order.builder()
                .id(100L)
                .userId(userId)
                .status(OrderStatus.SHIPPED)
                .build();

        when(orderRepository.findByIdAndUserId(100L, userId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> orderService.cancelOrder(100L, userId));
    }

    @Test
    void getOrder_deveRetornarPedidoDoUsuario() {
        String userId = "user-1";

        Order order = Order.builder()
                .id(100L)
                .userId(userId)
                .status(OrderStatus.CREATED)
                .totalItens(0)
                .totalValor(BigDecimal.ZERO)
                .itens(List.of())
                .build();

        when(orderRepository.findByIdAndUserId(100L, userId)).thenReturn(Optional.of(order));

        OrderResponseDTO response = orderService.getOrder(100L, userId);

        assertEquals(100L, response.getId());
        assertEquals(userId, response.getUserId());
    }
}

