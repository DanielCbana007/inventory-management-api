package inventory.management.api.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// OK [§4]: DTO de ENTRADA. No lleva id a proposito: aceptarlo seria mass assignment.
//          La validacion vive aqui, en el borde, que es donde el estandar la situa.
// MEJORA [§3]: sin @Schema con description y example, el formulario de Swagger UI sale
//        vacio y quien lo use escribe a ciegas. NO repitas ahi "obligatorio" ni "maximo 100":
//        springdoc lee @NotBlank y @Size y los refleja solos como required y maxLength.
public record CategoryRequestDto(
        @NotBlank @Size(max = 100) String name,
        String description
) {
}
