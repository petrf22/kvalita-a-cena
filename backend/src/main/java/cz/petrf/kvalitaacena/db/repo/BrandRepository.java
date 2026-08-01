package cz.petrf.kvalitaacena.db.repo;

import cz.petrf.kvalitaacena.db.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
