package inventory.management.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// OK [§4]: DTO de ENTRADA. No lleva id a proposito: aceptarlo seria mass assignment.
//          La validacion vive aqui, en el borde, que es donde el estandar la situa.
@Schema(name = "CategoryRequest", description = "Payload to create or replace a category")
public record CategoryRequestDto(
        @Schema(description = "Name of the category. Must be unique.",
                example = "Electronics")
        @NotBlank @Size(min = 1, max = 100) String name,

        @Schema(description = "Free-text description of the category.",
                example = "Electronic devices and gadgets")
        String description
) {
}
