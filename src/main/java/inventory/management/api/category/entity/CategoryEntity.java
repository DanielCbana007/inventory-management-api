package inventory.management.api.category.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;


@Entity
@Table(name = "tbl_category")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    public CategoryEntity() {
    }

    public CategoryEntity(String name, String description) {
        this.name = name;
        this.description = description;
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

    public void updateWith(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // TODO [§2.2]: crea la entidad Product y su relación con Category (@ManyToOne desde
    //              Product, @OneToMany aquí si la necesitas). Es una API de inventario y el
    //              producto todavía no existe: sin él no hay modelo ER que normalizar ni
    //              asociación JPA que demostrar.
}
