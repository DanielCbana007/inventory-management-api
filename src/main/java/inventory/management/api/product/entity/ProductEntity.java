package inventory.management.api.product.entity;

import inventory.management.api.category.entity.CategoryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tbl_product")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    // OK [§4]: precision/scale, 12 digitos y 2 decimales. Sin esto Postgres crea un numeric
    //     sin limites y el redondeo deja de estar bajo control. Dinero nunca en double.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // OK [§4]: LAZY explicito. @ManyToOne es EAGER por defecto, y eso trae la categoria en
    //     cada consulta la necesites o no. El listado, que si la usa, la pide en la misma
    //     consulta con @EntityGraph en ProductRepository.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    public ProductEntity() {
    }

    public ProductEntity(String name, String description, String sku, BigDecimal price,
                         int stock, CategoryEntity category) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    // El sku no entra: es el identificador comercial y no cambia. ProductService rechaza
    // con 409 un PUT que intente cambiarlo, en vez de ignorarlo en silencio.
    public void updateWith(String name, String description, BigDecimal price,
                           int stock, CategoryEntity category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    // OK [§4]: equals/hashCode por id, con guarda de null y hashCode constante. Es la forma
    //     correcta con ids generados: antes de persistir el id es null y dos entidades nuevas
    //     no deben ser iguales; el hashCode constante evita que la entidad se pierda dentro
    //     de un HashSet cuando Hibernate le asigna el id.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ProductEntity entity = (ProductEntity) obj;
        return id != null && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
