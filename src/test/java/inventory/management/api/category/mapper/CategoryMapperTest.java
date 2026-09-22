package inventory.management.api.category.mapper;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.category.dto.CategoryRequestDto;
import inventory.management.api.category.entity.CategoryEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Category Mapper")
class CategoryMapperTest {
    private final CategoryMapper mapper = new CategoryMapper();

    @Nested
    @DisplayName("toDto")
    class toDto {

        @Test
        @DisplayName("Should retund CategoryDto when parameter is CategoryEntity.")
        void shouldReturnCategoryDto() {
            // Arrange
            CategoryEntity entity = new CategoryEntity("ACTION", "Action");

            // Act
            CategoryDto result = mapper.toDto(entity);

            // Assert
            assertNotNull(result);
            assertEquals("ACTION", result.name());
            assertEquals("Action", result.description());
        }

    }

    @Nested
    @DisplayName("toEntity")
    class toEntity {

        @Test
        @DisplayName("Should retund CategoryEntity when parameter is CategoryRequestDto.")
        void shouldReturnCategoryEntity() {
            // Arrange
            CategoryRequestDto requestDto = new CategoryRequestDto("ACTION", "Action");

            // Act
            CategoryEntity result = mapper.toEntity(requestDto);

            // Assert
            assertNotNull(result);
            assertEquals("ACTION", result.getName());
            assertEquals("Action", result.getDescription());
        }

    }

}