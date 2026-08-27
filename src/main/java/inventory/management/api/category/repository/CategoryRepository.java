package inventory.management.api.category.repository;

import inventory.management.api.category.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// OK: interfaz que extiende JpaRepository con query methods derivados y sin
//     implementación manual. Correcto.
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    // BUG [§1.4c]: CAUSA RAÍZ del 500 al borrar. Devuelve null cuando no encuentra.
    //              Con Optional<CategoryEntity> el compilador te obligaría a tratar
    //              el caso "no existe" y el bug de aleteCategory sería imposible.
    CategoryEntity findByName(String name);

    // MEJORA: Boolean (objeto, puede ser null) donde basta boolean primitivo.
    Boolean existsByName(String name);

    // BUG [§1.4f]: código muerto Y roto a la vez. Nadie lo llama, y un deleteBy...
    //              derivado necesita @Transactional o falla en ejecución.
    void deleteByName(String name);
}
