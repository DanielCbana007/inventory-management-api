package inventory.management.api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
