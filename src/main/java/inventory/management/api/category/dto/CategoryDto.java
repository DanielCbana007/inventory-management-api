package inventory.management.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
