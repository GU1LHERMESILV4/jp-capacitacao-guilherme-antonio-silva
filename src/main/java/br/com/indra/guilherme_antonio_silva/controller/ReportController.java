package br.com.indra.guilherme_antonio_silva.controller;

import br.com.indra.guilherme_antonio_silva.repository.OrderRepository;
import br.com.indra.guilherme_antonio_silva.repository.ProdutosRepository;
import br.com.indra.guilherme_antonio_silva.model.Order;
import br.com.indra.guilherme_antonio_silva.model.OrderStatus;
import br.com.indra.guilherme_antonio_silva.model.Produtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
@Tag(name = "Relatórios", description = "Endpoints de relatórios e métricas")
public class ReportController {

    private final OrderRepository orderRepository;
    private final ProdutosRepository produtosRepository;

    public ReportController(OrderRepository orderRepository, ProdutosRepository produtosRepository) {
        this.orderRepository = orderRepository;
        this.produtosRepository = produtosRepository;
    }

    @GetMapping("/sales")
    @Operation(summary = "Faturamento por período")
    public ResponseEntity<BigDecimal> getSales(@RequestParam("from") OffsetDateTime from,
                                               @RequestParam("to") OffsetDateTime to) {
        BigDecimal total = orderRepository.findAll().stream()
                .filter(o -> (o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.DELIVERED))
                .filter(o -> !o.getCreatedAt().isBefore(from) && !o.getCreatedAt().isAfter(to))
                .map(Order::getTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(total);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Produtos com estoque baixo")
    public ResponseEntity<List<Produtos>> getLowStock(@RequestParam("threshold") Integer threshold) {
        List<Produtos> lowStock = produtosRepository.findAll().stream()
                .filter(p -> p.getQuantidadeEstoque() != null && p.getQuantidadeEstoque() <= threshold)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lowStock);
    }

    @GetMapping("/top-products")
    @Operation(summary = "Produtos mais vendidos (simplificado)")
    public ResponseEntity<List<Long>> getTopProducts() {
        Map<Long, Long> counts = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.DELIVERED)
                .flatMap(o -> o.getItens().stream())
                .collect(Collectors.groupingBy(i -> i.getProduto().getId(), Collectors.summingLong(i -> i.getQuantidade().longValue())));

        List<Long> top = counts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return ResponseEntity.ok(top);
    }
}

