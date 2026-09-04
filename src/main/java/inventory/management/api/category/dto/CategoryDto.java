package inventory.management.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// OK [§4]: DTO de SALIDA. Si lleva id: es el servidor diciendo al cliente como referirse
//          al recurso. Mismo campo que en el de entrada, direccion opuesta, regla opuesta.
@Schema(name = "Category", description = "A category as returned by the API")
public record CategoryDto(
        @Schema(description = "Identifier assigned by the database.",
                example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @Schema(description = "Unique name of the category.", example = "Electronics")
        String name,

        @Schema(description = "Free text explaining what the category groups.",
                example = "Electronic devices and gadgets")
        String description
) {
}
