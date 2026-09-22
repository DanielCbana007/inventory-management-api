package inventory.management.api.product.repository;

import inventory.management.api.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    @EntityGraph(attributePaths = "category")
    List<ProductEntity> findAll();
    boolean existsBySku(String sku);
}
