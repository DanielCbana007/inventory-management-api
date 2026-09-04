package inventory.management.api.product.dto;

import inventory.management.api.category.dto.CategoryDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "Product", description = "A product as returned by the API")
public record ProductDto(

        @Schema(description = "Identifier assigned by the database.",
                example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @Schema(description = "Commercial name of the product.",
                example = "Wireless keyboard K380")
        String name,

        @Schema(description = "Free-text description of the product.",
                example = "Bluetooth keyboard, multi-device, Spanish layout")
        String description,

        @Schema(description = "Stock keeping unit. Unique across the catalogue and never changes once created.",
                example = "LOG-K380-ES")
        String sku,

        @Schema(description = "Unit price in the catalogue currency. Never negative.",
                example = "39.90")
        BigDecimal price,

        @Schema(description = "Units currently available. Zero means out of stock, never negative.",
                example = "120")
        Integer stock,

        @Schema(description = "When the product was created. Set by the server.",
                example = "2026-09-04T10:15:30", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime createdAt,

        @Schema(description = "When the product was last modified. Set by the server on every update.",
                example = "2026-09-04T18:42:07", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime updatedAt,

        @Schema(description = "Category this product belongs to.")
        CategoryDto category
) {
}
