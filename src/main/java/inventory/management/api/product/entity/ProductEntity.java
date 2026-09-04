package inventory.management.api.product;

import inventory.management.api.category.entity.CategoryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/*
 * CRUD completo de Productos
 * (nombre,
 * SKU único,
 *  descripción,
 *  precio,
 *  cantidad en stock,
 *  categoría asociada,
 *  fecha de creación/actualización).
 * */

@Entity
@Table(name = "tbl_product")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private String stock;

    @Column(updatable = false, nullable = false)
    private LocalDateTime created_at;

    @Column(nullable = false)
    private LocalDateTime update_at;

    @ManyToOne
    @JoinColumn(name = "Category_id", nullable = false)
    private CategoryEntity category;

    public ProductEntity() {
    }

    public ProductEntity(String name, String description, String sku, double price, String stock, LocalDateTime created_at, LocalDateTime update_at, CategoryEntity category) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.price = price;
        this.stock = stock;
        this.created_at = created_at;
        this.update_at = update_at;
        this.category = category;
    }
}
