package inventory.management.api.product.Repository;

import inventory.management.api.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    boolean existsBySku(String sku);
}
