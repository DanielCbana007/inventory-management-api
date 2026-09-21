package inventory.management.api.product.Repository;

import inventory.management.api.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// ERROR [§1.4]: el paquete es product.Repository, en mayuscula. Contra la convencion de
//        nombres de Java (JLS 6.1 y la guia de Oracle: paquetes en minuscula) y contra los
//        otros cinco paquetes de product, que si lo son. Renombrar con IntelliJ.
// MEJORA [§3.1]: aqui se cierra el N+1 del listado, declarando que findAll traiga la
//         categoria en la misma consulta:
//             @EntityGraph(attributePaths = "category")
//             List<ProductEntity> findAll();
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    boolean existsBySku(String sku);
}
