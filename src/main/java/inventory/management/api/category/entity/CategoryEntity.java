package inventory.management.api.category.entity;

import jakarta.persistence.*;

// ERROR [§1.1]: esta clase viaja hasta el controller en los dos sentidos. Es el
//               origen del mass assignment y del acoplamiento contrato-esquema.
//               Debería morir en el service; hacia afuera van DTOs.
@Entity
// OK: nombre de tabla explícito, sin depender de la convención por defecto.
@Table(name = "tbl_category")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // OJO [§3.5]: data.sql inserta ids explícitos (1..4) sobre esta columna IDENTITY.
    //             La secuencia de Postgres sigue en 1, así que el primer POST que
    //             funcione bien chocará contra la fila 'Electrónica'.
    private Long id;

    // OK: la restricción está en la base... pero es la ÚNICA validación que existe.
    // FALTA [§1.5]: la validación va en el borde, en el DTO de entrada. Hoy un name
    //               nulo viaja hasta Postgres y vuelve como 500 en vez de 400.
    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // OK: constructor sin argumentos, requisito de JPA. Presente y consciente.
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

    // OK: no hay setId(). Bien, el id no se toca desde fuera.
    // MEJORA [§3.1]: solo getters y setters = modelo anémico. La lógica de negocio
    //                vive en el service en vez de en quien tiene los datos.
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // MEJORA [§3.5]: faltan equals() y hashCode(). Tiene efectos reales en JPA
    //                cuando la entidad entra en un Set o se compara entre sesiones.

    // FALTA [§2.5]: no hay relación con Product. Es una API de inventario y el
    //               producto todavía no existe: sin él no hay modelo ER que normalizar.
}
