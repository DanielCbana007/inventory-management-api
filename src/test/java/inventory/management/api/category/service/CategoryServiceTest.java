package inventory.management.api.category.service;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.category.dto.CategoryRequestDto;
import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.mapper.CategoryMapper;
import inventory.management.api.category.repository.CategoryRepository;
import inventory.management.api.exception.CusEntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@DisplayName("Category service")
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    private CategoryMapper mapper;

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryService service;

    @Test
    @DisplayName("should return the DTO with the id that save() assigned.")
    void createCategory() {
        // Arrange
        CategoryRequestDto requestDto = new CategoryRequestDto("ACTION", "Action.");
        CategoryEntity entity = new CategoryEntity("ACTION", "Action.");
        CategoryEntity savedEntity = new CategoryEntity("ACTION", "Action.");
        CategoryDto expectDto = new CategoryDto(1L, "ACTION", "Action.");

        when(mapper.toEntity(requestDto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDto(savedEntity)).thenReturn(expectDto);

        // Act
        CategoryDto result = service.createCategory(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ACTION", result.name());
        assertEquals("Action.", result.description());

    }

    @Test
    @DisplayName("Should return all categories.")
    void getAllCategories() {
        // Arrange
        List<CategoryEntity> entities = List.of(
                new CategoryEntity("ACTION", "Action."),
                new CategoryEntity("ANIMATED", "Animated.")
        );
        List<CategoryDto> categoryDtos =  List.of(
                new CategoryDto(1L, "ACTION", "Action."),
                new CategoryDto(2L, "ANIMATED", "Animated.")
        );

        when(repository.findAll()).thenReturn(entities);
        when(mapper.toDtoAll(entities)).thenReturn(categoryDtos);

        // Act
        List<CategoryDto> resultListDtos = service.getAllCategories();

        // Assert
        assertEquals(1L, resultListDtos.get(0).id());
        assertEquals(2L, resultListDtos.get(1).id());
        assertEquals(2, resultListDtos.size());
    }

    @Nested
    @DisplayName("updateCategory")
    class updateCategory {
        @Test
        @DisplayName("Should CusEntityNotFoundException when id not exists.")
        void updateCategoryException() {
            // Arrange
            CategoryRequestDto requestDto = new CategoryRequestDto("ACTION", "Action.");

            when(repository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CusEntityNotFoundException.class, () -> {
                service.updateCategory(requestDto, 1L);
            });
        }

        @Test
        @DisplayName("Should persistence new fileds.")
        void updatePersistence() {
            // Arrange
            CategoryRequestDto requestDto = new CategoryRequestDto("ACTION", "Action.");
            CategoryEntity entity = new CategoryEntity("ACIONN", "action");

            when(repository.findById(1L)).thenReturn(Optional.of(entity));
            when(mapper.toDto(entity)).thenReturn(new CategoryDto(1L, "ACTION", "Action."));

            // Act
            CategoryDto result = service.updateCategory(requestDto, 1L);

            // Assert
            assertNotNull(result);
            assertEquals("ACTION", result.name());
            assertEquals("Action.", result.description());
        }
    }

    @Test
    @DisplayName("Should retund CusEntityNotFoundException id the id not exists.")
    void deleteCategory() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CusEntityNotFoundException.class, () -> {
            service.deleteCategory(1L);
        });
    }
}