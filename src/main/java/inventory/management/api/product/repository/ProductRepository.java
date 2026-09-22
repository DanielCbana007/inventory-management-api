package inventory.management.api.product.repository;

import inventory.management.api.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// MEJORA [§3.1]: aqui se cierra el N+1 del listado, declarando que findAll traiga la
//         categoria en la misma consulta:
//             @EntityGraph(attributePaths = "category")
//             List<ProductEntity> findAll();
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    boolean existsBySku(String sku);
}
