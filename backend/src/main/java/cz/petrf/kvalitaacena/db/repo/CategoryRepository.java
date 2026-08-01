package cz.petrf.kvalitaacena.db.repo;

import cz.petrf.kvalitaacena.db.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
