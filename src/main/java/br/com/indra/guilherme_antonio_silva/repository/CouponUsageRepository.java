package br.com.indra.guilherme_antonio_silva.repository;

import br.com.indra.guilherme_antonio_silva.model.CouponUsage;
import br.com.indra.guilherme_antonio_silva.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    Optional<CouponUsage> findByPromotionAndUserId(Promotion promotion, String userId);
}

