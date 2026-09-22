package inventory.management.api.product.mapper;

import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.mapper.CategoryMapper;
import inventory.management.api.product.dto.ProductDto;
import inventory.management.api.product.dto.ProductRequestDto;
import inventory.management.api.product.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Product Mapper")
class ProductMapperTest {
    private final ProductMapper mapper = new ProductMapper(new CategoryMapper());

    private CategoryEntity electronics;

    @BeforeEach
    void setUp() {
        electronics = new CategoryEntity("ELECTRONICS", "Electronic items.");
        ReflectionTestUtils.setField(electronics, "id", 1L);
    }

    @Nested
    @DisplayName("toDto")
    class ToDto {

        @Test
        @DisplayName("Should copy every field and map the category to a CategoryDto.")
        void shouldReturnProductDto() {
            // Arrange
            ProductEntity entity = new ProductEntity("Keyboard K380", "Bluetooth keyboard", "LOG-K380",
                    new BigDecimal("39.90"), 120, electronics);
            ReflectionTestUtils.setField(entity, "id", 10L);

            // Act
            ProductDto result = mapper.toDto(entity);

            // Assert
            assertEquals(10L, result.id());
            assertEquals("Keyboard K380", result.name());
            assertEquals("Bluetooth keyboard", result.description());
            assertEquals("LOG-K380", result.sku());
            assertEquals(new BigDecimal("39.90"), result.price());
            assertEquals(120, result.stock());
            assertEquals(1L, result.category().id());
            assertEquals("ELECTRONICS", result.category().name());
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("Should build a new entity with the given category and no id.")
        void shouldReturnProductEntity() {
            // Arrange
            ProductRequestDto requestDto = new ProductRequestDto("Keyboard K380", "Bluetooth keyboard",
                    "LOG-K380", new BigDecimal("39.90"), 120, 1L);

            // Act
            ProductEntity result = mapper.toEntity(requestDto, electronics);

            // Assert
            assertNull(result.getId());
            assertEquals("Keyboard K380", result.getName());
            assertEquals("LOG-K380", result.getSku());
            assertEquals(new BigDecimal("39.90"), result.getPrice());
            assertEquals(120, result.getStock());
            assertSame(electronics, result.getCategory());
        }
    }
}
