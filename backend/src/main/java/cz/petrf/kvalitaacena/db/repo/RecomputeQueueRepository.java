package cz.petrf.kvalitaacena.db.repo;

import cz.petrf.kvalitaacena.db.entity.RecomputeQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecomputeQueueRepository extends JpaRepository<RecomputeQueue, Long> {

  List<RecomputeQueue> findTop200ByProcessedAtIsNullOrderByEnqueuedAtAsc();
}
