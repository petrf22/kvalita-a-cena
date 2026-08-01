package cz.petrf.kvalitaacena.db.repo;

import cz.petrf.kvalitaacena.db.entity.ObservationStatus;
import cz.petrf.kvalitaacena.db.entity.PriceObservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceObservationRepository extends JpaRepository<PriceObservation, Long> {

  List<PriceObservation> findByProductIdAndStoreIdAndStatus(Long productId, Long storeId, ObservationStatus status);
}
