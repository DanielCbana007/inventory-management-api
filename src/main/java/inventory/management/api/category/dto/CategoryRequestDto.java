package inventory.management.api.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDto(
        @NotBlank @Size(max = 100) String name,
        String description
) {
}
