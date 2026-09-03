package inventory.management.api.category.service;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.category.dto.CategoryRequestDto;
import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.mapper.CategoryMapper;
import inventory.management.api.category.repository.CategoryRepository;
import inventory.management.api.exception.CusEntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DisplayName("Category service")
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;
    private final CategoryMapper mapper = new CategoryMapper();

    private CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(repository, mapper);
    }

    @Test
    @DisplayName("should return the DTO with the id that save() assigned.")
    void createCategory() {
        // Arrange
        CategoryRequestDto requestDto = new CategoryRequestDto("ACTION", "Action.");

        CategoryEntity savedEntity = new CategoryEntity("ACTION", "Action.");
        ReflectionTestUtils.setField(savedEntity, "id", 1L);

        when(repository.existsByName("ACTION")).thenReturn(false);
        when(repository.save(any(CategoryEntity.class))).thenReturn(savedEntity);

        // Act
        CategoryDto result = service.createCategory(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("ACTION", result.name());
        assertEquals("Action.", result.description());
    }

    @Test
    @DisplayName("Should throw CusEntityAlreadyExistsException when the name already exists.")
    void createCategoryDuplicate() {
        // Arrange
        CategoryRequestDto requestDto = new CategoryRequestDto("ACTION", "Action.");

        when(repository.existsByName("ACTION")).thenReturn(true);

        // Act & Assert
        assertThrows(inventory.management.api.exception.CusEntityAlreadyExistsException.class,
                () -> service.createCategory(requestDto));

        verify(repository, never()).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("Should return all categories.")
    void getAllCategories() {
        // Arrange
        List<CategoryEntity> entities = List.of(
                new CategoryEntity("ACTION", "Action."),
                new CategoryEntity("ANIMATED", "Animated.")
        );

        when(repository.findAll()).thenReturn(entities);

        // Act
        List<CategoryDto> result = service.getAllCategories();

        // Assert
        assertEquals(2, result.size());
        assertEquals("ACTION", result.get(0).name());
        assertEquals("Action.", result.get(0).description());
        assertEquals("ANIMATED", result.get(1).name());
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {

        @Test
        @DisplayName("Should throw CusEntityNotFoundException when the id does not exist.")
        void updateCategoryNotFound() {
            // Arrange
            CategoryRequestDto requestDto = new CategoryRequestDto("ACTION", "Action.");

            when(repository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CusEntityNotFoundException.class,
                    () -> service.updateCategory(requestDto, 99L));
        }

        @Test
        @DisplayName("Should apply the new values to the entity.")
        void updatePersistence() {
            // Arrange
            CategoryRequestDto requestDto = new CategoryRequestDto("ACTION", "Action.");
            CategoryEntity entity = new CategoryEntity("ACIONN", "action");

            when(repository.findById(1L)).thenReturn(Optional.of(entity));

            // Act
            service.updateCategory(requestDto, 1L);

            // Assert
            assertEquals("ACTION", entity.getName());
            assertEquals("Action.", entity.getDescription());
        }

        @Test
        @DisplayName("Should return the UPDATED values, not the previous ones.")
        void updateReturnsNewValues() {
            // Arrange
            CategoryRequestDto requestDto = new CategoryRequestDto("ACTION", "Action.");
            CategoryEntity entity = new CategoryEntity("ACIONN", "action");

            when(repository.findById(1L)).thenReturn(Optional.of(entity));

            // Act
            CategoryDto result = service.updateCategory(requestDto, 1L);

            // Assert
            assertEquals("ACTION", result.name());
            assertEquals("Action.", result.description());
        }
    }

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategory {

        @Test
        @DisplayName("Should throw CusEntityNotFoundException when the id does not exist.")
        void deleteCategoryNotFound() {
            // Arrange
            when(repository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CusEntityNotFoundException.class,
                    () -> service.deleteCategory(99L));

            verify(repository, never()).delete(any(CategoryEntity.class));
        }

        @Test
        @DisplayName("Should delete the entity found by id.")
        void deleteCategoryOk() {
            // Arrange
            CategoryEntity entity = new CategoryEntity("ACTION", "Action.");

            when(repository.findById(1L)).thenReturn(Optional.of(entity));

            // Act
            service.deleteCategory(1L);

            // Assert
            verify(repository).delete(entity);
        }
    }
}
