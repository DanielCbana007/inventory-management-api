package inventory.management.api.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(name = "ProductRequest", description = "Payload to create or replace a product")
public record ProductRequestDto(

        @Schema(description = "Commercial name of the product.",
                example = "Wireless keyboard K380")
        @NotBlank @Size(min = 3, max = 100)
        String name,

        @Schema(description = "Free-text description of the product.",
                example = "Bluetooth keyboard, multi-device, Spanish layout")
        @NotBlank @Size(min = 1, max = 500)
        String description,

        @Schema(description = "Stock keeping unit. Must be unique across the catalogue.",
                example = "LOG-K380-ES")
        @NotBlank @Size(min = 1, max = 50)
        String sku,

        @Schema(description = "Unit price in the catalogue currency. Cannot be negative.",
                example = "39.90")
        @NotNull @DecimalMin(value = "0.00")
        BigDecimal price,

        @Schema(description = "Units available. Zero is valid and means out of stock.",
                example = "120")
        @NotNull @PositiveOrZero
        Integer stock,

        @Schema(description = "Id of an existing category this product belongs to.",
                example = "3")
        @NotNull
        Long categoryId
) {
}
