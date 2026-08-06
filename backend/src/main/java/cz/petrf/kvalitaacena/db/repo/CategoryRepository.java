package cz.petrf.kvalitaacena.db.repo;

import cz.petrf.kvalitaacena.db.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  /** Plochý seznam pro formuláře zakládání zboží — path drží pořadí podle stromu. */
  List<Category> findAllByOrderByPathAsc();
}
