package cz.petrf.kvalitaacena.db.repo;

import cz.petrf.kvalitaacena.db.entity.PriceCurrent;
import cz.petrf.kvalitaacena.db.entity.PriceCurrentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceCurrentRepository extends JpaRepository<PriceCurrent, PriceCurrentId> {

  List<PriceCurrent> findByProductId(Long productId);
}
