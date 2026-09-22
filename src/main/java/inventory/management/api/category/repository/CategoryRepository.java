package inventory.management.api.category.repository;

import inventory.management.api.category.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByName(String name);

    // OK [§4]: Postgres lo resuelve con un EXISTS. getProducts().isEmpty() cargaria en memoria
    //     todos los productos de la categoria solo para saber si hay alguno.
    boolean existsByIdAndProductsIsNotEmpty(Long id);
}
