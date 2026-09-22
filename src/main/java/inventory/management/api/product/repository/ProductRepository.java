package inventory.management.api.product.repository;

import inventory.management.api.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    // OK [§4]: el N+1 del listado, resuelto y medido: 5 SELECT -> 1 con JOIN (2 cuando la pagina
    //     no cubre el total: la pagina y el count). Es JOIN y no LEFT JOIN por optional = false.
    // MEJORA [§3.4]: findById no lleva el grafo, asi que GET /products/{id} hace 2 consultas
    //         (producto + categoria). Es un 1+1 fijo, no un N+1; el mismo @EntityGraph lo deja en 1.
    @EntityGraph(attributePaths = "category")
    Page<ProductEntity> findAll(Pageable pageable);
    boolean existsBySku(String sku);
}
