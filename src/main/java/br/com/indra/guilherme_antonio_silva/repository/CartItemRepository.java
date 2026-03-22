package br.com.indra.guilherme_antonio_silva.repository;

import br.com.indra.guilherme_antonio_silva.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByIdAndCartUserIdAndCartAtivoTrue(Long id, String userId);
}

