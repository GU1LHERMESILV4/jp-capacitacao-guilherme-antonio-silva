package br.com.indra.guilherme_antonio_silva.service;

import br.com.indra.guilherme_antonio_silva.model.Cart;
import br.com.indra.guilherme_antonio_silva.model.CartItem;
import br.com.indra.guilherme_antonio_silva.model.Produtos;
import br.com.indra.guilherme_antonio_silva.repository.CartItemRepository;
import br.com.indra.guilherme_antonio_silva.repository.CartRepository;
import br.com.indra.guilherme_antonio_silva.repository.ProdutosRepository;
import br.com.indra.guilherme_antonio_silva.dto.CartItemRequestDTO;
import br.com.indra.guilherme_antonio_silva.dto.CartItemResponseDTO;
import br.com.indra.guilherme_antonio_silva.dto.CartResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProdutosRepository produtosRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProdutosRepository produtosRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.produtosRepository = produtosRepository;
    }

    @Transactional
    public CartResponseDTO getOrCreateActiveCart(String userId) {
        Cart cart = cartRepository.findByUserIdAndAtivoTrue(userId)
                .orElseGet(() -> {
                    Cart novo = new Cart();
                    novo.setUserId(userId);
                    novo.setAtivo(true);
                    novo.setTotalItens(0);
                    novo.setTotalValor(BigDecimal.ZERO);
                    return cartRepository.save(novo);
                });

        return toResponseDTO(cart);
    }

    @Transactional
    public CartResponseDTO addItem(String userId, CartItemRequestDTO dto) {
        Cart cart = cartRepository.findByUserIdAndAtivoTrue(userId)
                .orElseGet(() -> {
                    Cart novo = new Cart();
                    novo.setUserId(userId);
                    novo.setAtivo(true);
                    novo.setTotalItens(0);
                    novo.setTotalValor(BigDecimal.ZERO);
                    return cartRepository.save(novo);
                });

        Produtos produto = produtosRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado para o id: " + dto.getProdutoId()));

        int quantidade = dto.getQuantidade() != null ? dto.getQuantidade() : 1;
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        BigDecimal precoUnitario = dto.getPrecoUnitario() != null
                ? dto.getPrecoUnitario()
                : produto.getPreco();

        CartItem item = CartItem.builder()
                .cart(cart)
                .produto(produto)
                .quantidade(quantidade)
                .precoUnitarioSnapshot(precoUnitario)
                .totalLinha(precoUnitario.multiply(BigDecimal.valueOf(quantidade)))
                .build();

        cart.getItens().add(item);
        recalcTotals(cart);

        cartRepository.save(cart);

        return toResponseDTO(cart);
    }

    @Transactional
    public CartResponseDTO updateItem(String userId, Long itemId, CartItemRequestDTO dto) {
        CartItem item = cartItemRepository.findByIdAndCartUserIdAndCartAtivoTrue(itemId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Item de carrinho não encontrado para o id: " + itemId));

        int quantidade = dto.getQuantidade() != null ? dto.getQuantidade() : item.getQuantidade();
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        BigDecimal precoUnitario = dto.getPrecoUnitario() != null
                ? dto.getPrecoUnitario()
                : item.getPrecoUnitarioSnapshot();

        item.setQuantidade(quantidade);
        item.setPrecoUnitarioSnapshot(precoUnitario);
        item.setTotalLinha(precoUnitario.multiply(BigDecimal.valueOf(quantidade)));

        Cart cart = item.getCart();
        recalcTotals(cart);

        cartRepository.save(cart);

        return toResponseDTO(cart);
    }

    @Transactional
    public CartResponseDTO removeItem(String userId, Long itemId) {
        CartItem item = cartItemRepository.findByIdAndCartUserIdAndCartAtivoTrue(itemId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Item de carrinho não encontrado para o id: " + itemId));

        Cart cart = item.getCart();
        cart.getItens().remove(item);
        cartItemRepository.delete(item);

        recalcTotals(cart);
        cartRepository.save(cart);

        return toResponseDTO(cart);
    }

    private void recalcTotals(Cart cart) {
        int totalItens = cart.getItens().stream()
                .mapToInt(CartItem::getQuantidade)
                .sum();

        BigDecimal totalValor = cart.getItens().stream()
                .map(CartItem::getTotalLinha)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalItens(totalItens);
        cart.setTotalValor(totalValor);
    }

    private CartResponseDTO toResponseDTO(Cart cart) {
        List<CartItemResponseDTO> itens = cart.getItens().stream()
                .map(item -> CartItemResponseDTO.builder()
                        .id(item.getId())
                        .produtoId(item.getProduto().getId())
                        .produtoNome(item.getProduto().getNome())
                        .quantidade(item.getQuantidade())
                        .precoUnitarioSnapshot(item.getPrecoUnitarioSnapshot())
                        .totalLinha(item.getTotalLinha())
                        .build())
                .toList();

        return CartResponseDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .ativo(cart.isAtivo())
                .totalItens(cart.getTotalItens())
                .totalValor(cart.getTotalValor())
                .itens(itens)
                .build();
    }
}

