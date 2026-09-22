package inventory.management.api.product.service;

import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.mapper.CategoryMapper;
import inventory.management.api.category.repository.CategoryRepository;
import inventory.management.api.exception.CusEntityAlreadyExistsException;
import inventory.management.api.exception.CusEntityConflictException;
import inventory.management.api.exception.CusEntityNotFoundException;
import inventory.management.api.product.dto.ProductDto;
import inventory.management.api.product.dto.ProductRequestDto;
import inventory.management.api.product.entity.ProductEntity;
import inventory.management.api.product.mapper.ProductMapper;
import inventory.management.api.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Product service")
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    private final ProductMapper mapper = new ProductMapper(new CategoryMapper());

    private ProductService service;

    private CategoryEntity electronics;
    private CategoryEntity clothing;

    @BeforeEach
    void setUp() {
        service = new ProductService(productRepository, categoryRepository, mapper);

        electronics = category(1L, "ELECTRONICS");
        clothing = category(2L, "CLOTHING");
    }

    private static CategoryEntity category(Long id, String name) {
        CategoryEntity category = new CategoryEntity(name, name + " items.");
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private static ProductRequestDto request(Long categoryId) {
        return new ProductRequestDto("Keyboard K380", "Bluetooth keyboard", "LOG-K380",
                new BigDecimal("39.90"), 120, categoryId);
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("Should return the DTO with the id that save() assigned and its category.")
        void createProduct() {
            // Arrange
            ProductEntity saved = new ProductEntity("Keyboard K380", "Bluetooth keyboard", "LOG-K380",
                    new BigDecimal("39.90"), 120, electronics);
            ReflectionTestUtils.setField(saved, "id", 10L);

            when(productRepository.existsBySku("LOG-K380")).thenReturn(false);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));
            when(productRepository.save(any(ProductEntity.class))).thenReturn(saved);

            // Act
            ProductDto result = service.createProduct(request(1L));

            // Assert
            assertEquals(10L, result.id());
            assertEquals("LOG-K380", result.sku());
            assertEquals(new BigDecimal("39.90"), result.price());
            assertEquals(1L, result.category().id());
        }

        @Test
        @DisplayName("Should throw CusEntityAlreadyExistsException when the sku already exists.")
        void createProductDuplicateSku() {
            // Arrange
            when(productRepository.existsBySku("LOG-K380")).thenReturn(true);

            // Act & Assert
            CusEntityAlreadyExistsException ex = assertThrows(CusEntityAlreadyExistsException.class,
                    () -> service.createProduct(request(1L)));

            assertTrue(ex.getMessage().contains("sku 'LOG-K380'"));
            verify(productRepository, never()).save(any(ProductEntity.class));
        }

        @Test
        @DisplayName("Should throw CusEntityNotFoundException when the category does not exist.")
        void createProductCategoryNotFound() {
            // Arrange
            when(productRepository.existsBySku("LOG-K380")).thenReturn(false);
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CusEntityNotFoundException.class,
                    () -> service.createProduct(request(99L)));

            verify(productRepository, never()).save(any(ProductEntity.class));
        }
    }

    @Test
    @DisplayName("Should return the requested page with each category, keeping the total.")
    void getAllProducts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);
        List<ProductEntity> entities = List.of(
                new ProductEntity("Keyboard", "d", "SKU-1", new BigDecimal("10.00"), 5, electronics),
                new ProductEntity("T-shirt", "d", "SKU-2", new BigDecimal("20.00"), 8, clothing)
        );

        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(entities, pageable, 7));

        // Act
        Page<ProductDto> result = service.getAllProducts(pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals("SKU-1", result.getContent().get(0).sku());
        assertEquals("ELECTRONICS", result.getContent().get(0).category().name());
        assertEquals("CLOTHING", result.getContent().get(1).category().name());
        assertEquals(7, result.getTotalElements());
    }

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("Should return the DTO of the product found, with its category.")
        void getProductByIdOk() {
            // Arrange
            ProductEntity product = new ProductEntity("Keyboard", "d", "LOG-K380",
                    new BigDecimal("39.90"), 120, electronics);
            ReflectionTestUtils.setField(product, "id", 10L);

            when(productRepository.findById(10L)).thenReturn(Optional.of(product));

            // Act
            ProductDto result = service.getProductById(10L);

            // Assert
            assertEquals(10L, result.id());
            assertEquals("LOG-K380", result.sku());
            assertEquals("ELECTRONICS", result.category().name());
        }

        @Test
        @DisplayName("Should throw CusEntityNotFoundException when the id does not exist.")
        void getProductByIdNotFound() {
            // Arrange
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CusEntityNotFoundException.class,
                    () -> service.getProductById(99L));
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("Should throw CusEntityNotFoundException when the product does not exist.")
        void updateProductNotFound() {
            // Arrange
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CusEntityNotFoundException.class,
                    () -> service.updateProduct(request(1L), 99L));

            verify(categoryRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw CusEntityConflictException and change nothing when the sku differs.")
        void updateProductSkuChanged() {
            // Arrange
            ProductEntity product = new ProductEntity("Old", "old", "LOG-K380",
                    new BigDecimal("1.00"), 1, electronics);
            ProductRequestDto changedSku = new ProductRequestDto("Keyboard K380", "Bluetooth keyboard",
                    "OTHER-SKU", new BigDecimal("39.90"), 120, 1L);

            when(productRepository.findById(10L)).thenReturn(Optional.of(product));

            // Act & Assert
            CusEntityConflictException ex = assertThrows(CusEntityConflictException.class,
                    () -> service.updateProduct(changedSku, 10L));

            assertTrue(ex.getMessage().contains("sku"));
            assertEquals("Old", product.getName());
            verify(categoryRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw CusEntityNotFoundException when the new category does not exist.")
        void updateProductCategoryNotFound() {
            // Arrange
            ProductEntity product = new ProductEntity("Old", "old", "LOG-K380",
                    new BigDecimal("1.00"), 1, electronics);

            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CusEntityNotFoundException.class,
                    () -> service.updateProduct(request(99L), 10L));

            assertEquals("Old", product.getName());
        }

        @Test
        @DisplayName("Should return the UPDATED values, not the previous ones.")
        void updateProductReturnsNewValues() {
            // Arrange
            ProductEntity product = new ProductEntity("Old", "old", "LOG-K380",
                    new BigDecimal("1.00"), 1, electronics);

            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));

            // Act
            ProductDto result = service.updateProduct(request(1L), 10L);

            // Assert
            verify(productRepository).flush();
            assertEquals("Keyboard K380", result.name());
            assertEquals(new BigDecimal("39.90"), result.price());
            assertEquals(120, result.stock());
            assertEquals("Keyboard K380", product.getName());
        }

        @Test
        @DisplayName("Should move the product to the new category.")
        void updateProductChangesCategory() {
            // Arrange
            ProductEntity product = new ProductEntity("Old", "old", "LOG-K380",
                    new BigDecimal("1.00"), 1, electronics);

            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(clothing));

            // Act
            ProductDto result = service.updateProduct(request(2L), 10L);

            // Assert
            assertSame(clothing, product.getCategory());
            assertEquals(2L, result.category().id());
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("Should throw CusEntityNotFoundException when the id does not exist.")
        void deleteProductNotFound() {
            // Arrange
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CusEntityNotFoundException.class,
                    () -> service.deleteProduct(99L));

            verify(productRepository, never()).delete(any(ProductEntity.class));
        }

        @Test
        @DisplayName("Should delete the entity found by id.")
        void deleteProductOk() {
            // Arrange
            ProductEntity product = new ProductEntity("Keyboard", "d", "LOG-K380",
                    new BigDecimal("1.00"), 1, electronics);

            when(productRepository.findById(10L)).thenReturn(Optional.of(product));

            // Act
            service.deleteProduct(10L);

            // Assert
            verify(productRepository).delete(product);
        }
    }
}
