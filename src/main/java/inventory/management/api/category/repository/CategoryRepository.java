package inventory.management.api.category.repository;

import inventory.management.api.category.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// OK [§4]: interfaz que extiende JpaRepository, sin implementacion manual y sin metodos
//          muertos. existsByName es un query method derivado: Spring Data genera la consulta
//          a partir del nombre.
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByName(String name);
}
