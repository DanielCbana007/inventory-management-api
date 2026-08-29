package inventory.management.api.category.repository;

import inventory.management.api.category.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    // TODO [§1.1]: declara boolean existsByName(String name); lo necesita
    //              CategoryService.createCategory para devolver un 409 con un mensaje
    //              que diga qué nombre chocó.
}
