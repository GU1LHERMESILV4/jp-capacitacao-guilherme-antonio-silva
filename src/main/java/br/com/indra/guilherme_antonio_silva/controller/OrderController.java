package br.com.indra.guilherme_antonio_silva.controller;

import br.com.indra.guilherme_antonio_silva.service.OrderService;
import br.com.indra.guilherme_antonio_silva.service.dto.OrderCreateRequestDTO;
import br.com.indra.guilherme_antonio_silva.service.dto.OrderResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@Tag(name = "Pedidos", description = "Endpoints para gerenciamento de pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Criar pedido a partir do carrinho ativo (checkout)")
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestHeader("X-User-Id") String userId,
                                                        @RequestBody(required = false) OrderCreateRequestDTO dto) {
        OrderResponseDTO response = orderService.createOrderFromCart(userId, dto != null ? dto : new OrderCreateRequestDTO());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por id")
    public ResponseEntity<OrderResponseDTO> getOrder(@RequestHeader("X-User-Id") String userId,
                                                     @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id, userId));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar pedido")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@RequestHeader("X-User-Id") String userId,
                                                        @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id, userId));
    }
}

