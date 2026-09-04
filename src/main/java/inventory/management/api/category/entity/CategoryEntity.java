package inventory.management.api.category.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import java.util.Objects;

@Entity
@Table(name = "tbl_category")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // la restriccion unique es la garantia real de unicidad frente a la condicion de
    //     carrera del service. nullable y length protegen la integridad por cualquier via.
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

    // un unico metodo que recibe el cambio completo, en vez de setters sueltos.
    //     La responsabilidad de modificar los datos vive en quien los tiene
    //     (GRASP Experto en Informacion). Si aparece una regla -normalizar el nombre,
    //     por ejemplo- este es el sitio, y valdria para cualquier via de entrada.
    public void updateWith(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // patron correcto para entidades JPA. Compara por id con guarda de null
    //     (una entidad sin persistir no es igual a ninguna otra) y hashCode constante
    //     para la clase, para que meterla en un Set antes de guardarla y recibir el id
    //     despues no la vuelva irrecuperable.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        CategoryEntity entity = (CategoryEntity) obj;
        return id != null && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // FALTA [§2.2]: no hay entidad Product ni relacion con Category (@ManyToOne desde
    //        Product). Es una API de inventario y el producto no existe: sin el no hay modelo
    //        ER que normalizar ni asociacion JPA que demostrar. Es lo que mas superficie nueva
    //        del plan abre.
}
