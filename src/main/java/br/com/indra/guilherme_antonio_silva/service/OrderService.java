package br.com.indra.guilherme_antonio_silva.service;

import br.com.indra.guilherme_antonio_silva.model.*;
import br.com.indra.guilherme_antonio_silva.repository.*;
import br.com.indra.guilherme_antonio_silva.service.dto.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventarioService inventarioService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        InventarioService inventarioService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.inventarioService = inventarioService;
    }

    @Transactional
    public OrderResponseDTO createOrderFromCart(String userId, OrderCreateRequestDTO dto) {
        Cart cart = cartRepository.findByUserIdAndAtivoTrue(userId)
                .orElseThrow(() -> new EntityNotFoundException("Carrinho ativo não encontrado para o usuário: " + userId));

        if (cart.getItens().isEmpty()) {
            throw new IllegalStateException("Carrinho vazio. Não é possível realizar checkout.");
        }

        // Criar pedido base
        Order order = Order.builder()
                .userId(cart.getUserId())
                .status(OrderStatus.CREATED)
                .cartId(cart.getId())
                .totalItens(cart.getTotalItens())
                .totalValor(cart.getTotalValor())
                .discount(BigDecimal.ZERO)
                .freight(BigDecimal.ZERO)
                .total(cart.getTotalValor())
                .address(dto != null ? dto.getAddress() : null)
                .createdAt(OffsetDateTime.now())
                .build();

        // Criar itens de pedido a partir dos itens do carrinho
        List<OrderItem> orderItems = cart.getItens().stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .produto(cartItem.getProduto())
                        .quantidade(cartItem.getQuantidade())
                        .precoUnitarioSnapshot(cartItem.getPrecoUnitarioSnapshot())
                        .totalLinha(cartItem.getTotalLinha())
                        .build())
                .toList();

        order.setItens(orderItems);

        // Abater estoque para cada item
        orderItems.forEach(item -> {
            InventarioAdjustmentRequestDTO adjustment = new InventarioAdjustmentRequestDTO();
            adjustment.setQuantidade(item.getQuantidade());
            adjustment.setDescricao("Reserva de estoque para pedido");
            inventarioService.removerEstoque(item.getProduto().getId(), adjustment);
        });

        // Persistir pedido
        Order savedOrder = orderRepository.save(order);

        // Marcar carrinho como inativo
        cart.setAtivo(false);
        cartRepository.save(cart);

        return toResponseDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(Long id, String userId) {
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado para o id: " + id));
        return toResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Long id, String userId) {
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado para o id: " + id));

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Cancelamento permitido apenas para pedidos nos status CREATED ou PAID");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(OffsetDateTime.now());

        // Devolver estoque de cada item
        order.getItens().forEach(item -> {
            InventarioAdjustmentRequestDTO adjustment = new InventarioAdjustmentRequestDTO();
            adjustment.setQuantidade(item.getQuantidade());
            adjustment.setDescricao("Devolução de estoque por cancelamento de pedido");
            inventarioService.adicionarEstoque(item.getProduto().getId(), adjustment);
        });

        Order saved = orderRepository.save(order);
        return toResponseDTO(saved);
    }

    private OrderResponseDTO toResponseDTO(Order order) {
        List<OrderItemResponseDTO> itens = order.getItens().stream()
                .map(item -> OrderItemResponseDTO.builder()
                        .id(item.getId())
                        .produtoId(item.getProduto().getId())
                        .produtoNome(item.getProduto().getNome())
                        .quantidade(item.getQuantidade())
                        .precoUnitarioSnapshot(item.getPrecoUnitarioSnapshot())
                        .totalLinha(item.getTotalLinha())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .cartId(order.getCartId())
                .totalItens(order.getTotalItens())
                .totalValor(order.getTotalValor())
                .discount(order.getDiscount())
                .freight(order.getFreight())
                .total(order.getTotal())
                .address(order.getAddress())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .itens(itens)
                .build();
    }
}

