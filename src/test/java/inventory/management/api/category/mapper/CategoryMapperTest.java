package inventory.management.api.category.mapper;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.category.dto.CategoryRequestDto;
import inventory.management.api.category.entity.CategoryEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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
            assertEquals("ACTION", entity.getName());
            assertEquals("Action", entity.getDescription());
        }

    }

    @Nested
    @DisplayName("toDtoAll")
    class toDtoAll {

        @Test
        @DisplayName("Should retund a list to CategoryDto when parameter is a list to CategoryEntity.")
        void shouldReturnListCategoryDto() {
            // Arrange
            List<CategoryEntity> entities = new ArrayList<>();
            entities.add(new CategoryEntity("ACTION", "Action"));
            entities.add(new CategoryEntity("ANIMATED", "Animated"));

            // Act
            List<CategoryDto> result = mapper.toDtoAll(entities);

            // Assert
            assertEquals(2, result.size());
            assertEquals("ACTION", result.get(0).name());
            assertEquals("Action", result.get(0).description());
            assertEquals("ANIMATED", result.get(1).name());
            assertEquals("Animated", result.get(1).description());
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