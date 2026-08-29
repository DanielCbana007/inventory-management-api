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

    // MEJORA: data.sql inserta ids explícitos (1..4) sobre esta columna IDENTITY y la
    //         secuencia de Postgres no avanza. Hoy no falla porque ya se pasó de 4, pero
    //         si recreas la base el primer POST chocará. Quita los ids del seed o ajusta
    //         la secuencia con setval().
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

    // MEJORA: sustituye estos dos setters por un updateWith(name, description) que
    //         encapsule el cambio. Hoy la clase es un saco de datos y la lógica vive
    //         en el service: eso es un modelo anémico.
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // MEJORA: implementa equals() y hashCode() basados en el id. Sin ellos, dos instancias
    //         de la misma fila no se consideran iguales al entrar en un Set o al compararse
    //         entre sesiones de Hibernate distintas.

    // TODO [§2.2]: crea la entidad Product y su relación con Category (@ManyToOne desde
    //              Product, @OneToMany aquí si la necesitas). Es una API de inventario y el
    //              producto todavía no existe: sin él no hay modelo ER que normalizar ni
    //              asociación JPA que demostrar.
}
