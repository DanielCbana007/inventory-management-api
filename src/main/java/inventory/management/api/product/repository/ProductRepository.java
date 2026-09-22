package inventory.management.api.product.repository;

import inventory.management.api.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    @EntityGraph(attributePaths = "category")
    Page<ProductEntity> findAll(Pageable pageable);
    boolean existsBySku(String sku);
}
