package inventory.management.api.product.controller;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.exception.CusEntityAlreadyExistsException;
import inventory.management.api.exception.CusEntityConflictException;
import inventory.management.api.exception.CusEntityNotFoundException;
import inventory.management.api.product.dto.ProductDto;
import inventory.management.api.product.dto.ProductRequestDto;
import inventory.management.api.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    private static final String PATH = "/api/v1/products";
    private static final String PROBLEM_JSON = "application/problem+json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    @Autowired
    private ObjectMapper objectMapper;

    private final ProductRequestDto request = new ProductRequestDto("Keyboard K380",
            "Bluetooth keyboard", "LOG-K380", new BigDecimal("39.90"), 120, 1L);

    private static ProductDto response(Long id, String sku) {
        LocalDateTime now = LocalDateTime.of(2026, 9, 21, 10, 0);
        return new ProductDto(id, "Keyboard K380", "Bluetooth keyboard", sku,
                new BigDecimal("39.90"), 120, now, now,
                new CategoryDto(1L, "ELECTRONICS", "Electronic items."));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("POST should return 201 with Location header and the category")
        void createReturn201() throws Exception {
            // Arrange
            when(service.createProduct(any(ProductRequestDto.class))).thenReturn(response(10L, "LOG-K380"));

            // Act & Assert
            mockMvc.perform(post(PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/products/10")))
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.sku").value("LOG-K380"))
                    .andExpect(jsonPath("$.category.id").value(1));
        }

        @Test
        @DisplayName("POST should return 409 when the sku already exists")
        void createReturn409() throws Exception {
            // Arrange
            when(service.createProduct(any(ProductRequestDto.class)))
                    .thenThrow(CusEntityAlreadyExistsException.of("Product", "sku", "LOG-K380"));

            // Act & Assert
            mockMvc.perform(post(PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value(containsString("LOG-K380")));
        }

        @Test
        @DisplayName("POST should return 404 when the category does not exist")
        void createReturn404() throws Exception {
            // Arrange
            when(service.createProduct(any(ProductRequestDto.class)))
                    .thenThrow(CusEntityNotFoundException.of("Category", 99L));

            // Act & Assert
            mockMvc.perform(post(PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value(containsString("99")));
        }

        @Test
        @DisplayName("POST should return 400 when price and stock are negative")
        void createReturn400() throws Exception {
            // Arrange
            ProductRequestDto invalid = new ProductRequestDto("Keyboard K380", "Bluetooth keyboard",
                    "LOG-K380", new BigDecimal("-1.00"), -5, 1L);

            // Act & Assert
            mockMvc.perform(post(PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(PROBLEM_JSON))
                    .andExpect(jsonPath("$.errors", hasSize(2)))
                    .andExpect(jsonPath("$.errors[*].code", hasItems("DecimalMin", "PositiveOrZero")));

            verify(service, never()).createProduct(any());
        }

        @Test
        @DisplayName("POST should return 400 when the price has more decimals than its column")
        void createReturn400PriceScale() throws Exception {
            // Arrange
            ProductRequestDto threeDecimals = new ProductRequestDto("Keyboard K380", "Bluetooth keyboard",
                    "LOG-K380", new BigDecimal("1.999"), 120, 1L);

            // Act & Assert
            mockMvc.perform(post(PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(threeDecimals)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("price"))
                    .andExpect(jsonPath("$.errors[0].code").value("Digits"));

            verify(service, never()).createProduct(any());
        }

        @Test
        @DisplayName("POST should return 400 when the price has more integer digits than its column")
        void createReturn400PricePrecision() throws Exception {
            // Arrange
            ProductRequestDto tooBig = new ProductRequestDto("Keyboard K380", "Bluetooth keyboard",
                    "LOG-K380", new BigDecimal("123456789012.00"), 120, 1L);

            // Act & Assert
            mockMvc.perform(post(PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tooBig)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].code").value("Digits"));

            verify(service, never()).createProduct(any());
        }

        @Test
        @DisplayName("POST should return 400 when the JSON is malformed")
        void createReturn400MalformedJson() throws Exception {
            // Act & Assert
            mockMvc.perform(post(PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Keyboard\", "))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(PROBLEM_JSON));

            verify(service, never()).createProduct(any());
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("GET should return 200 with the page content and its metadata")
        void getAllReturn200() throws Exception {
            // Arrange
            when(service.getAllProducts(any(Pageable.class))).thenReturn(new PageImpl<>(
                    List.of(response(10L, "SKU-1"), response(11L, "SKU-2")), PageRequest.of(0, 2), 7));

            // Act & Assert
            mockMvc.perform(get(PATH + "?size=2"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].sku").value("SKU-1"))
                    .andExpect(jsonPath("$.content[1].sku").value("SKU-2"))
                    .andExpect(jsonPath("$.page.totalElements").value(7))
                    .andExpect(jsonPath("$.page.totalPages").value(4));
        }

        @Test
        @DisplayName("GET without params should ask for page 0, size 20, ordered by id")
        void getAllDefaultPageable() throws Exception {
            // Arrange
            when(service.getAllProducts(any(Pageable.class))).thenReturn(Page.empty());

            // Act
            mockMvc.perform(get(PATH)).andExpect(status().isOk());

            // Assert
            verify(service).getAllProducts(PageRequest.of(0, 20, Sort.by("id")));
        }

        @Test
        @DisplayName("GET should return 400 when sort is not a sortable field")
        void getAllUnsortableField400() throws Exception {
            // Act & Assert
            mockMvc.perform(get(PATH + "?sort=precio"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value(containsString("precio")));

            verify(service, never()).getAllProducts(any());
        }

        @Test
        @DisplayName("PATCH on the collection should return 405")
        void patchReturn405() throws Exception {
            // Act & Assert
            mockMvc.perform(patch(PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(content().contentType(PROBLEM_JSON));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("GET /{id} should return 200 with the product and its category")
        void getByIdReturn200() throws Exception {
            // Arrange
            when(service.getProductById(10L)).thenReturn(response(10L, "LOG-K380"));

            // Act & Assert
            mockMvc.perform(get(PATH + "/10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.category.name").value("ELECTRONICS"));
        }

        @Test
        @DisplayName("GET /{id} should return 404 when the product does not exist")
        void getByIdReturn404() throws Exception {
            // Arrange
            when(service.getProductById(99L)).thenThrow(CusEntityNotFoundException.of("Product", 99L));

            // Act & Assert
            mockMvc.perform(get(PATH + "/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value(containsString("99")));
        }

        @Test
        @DisplayName("GET /{id} should return 400 when the id is not a valid number")
        void getByIdReturn400() throws Exception {
            // Act & Assert
            mockMvc.perform(get(PATH + "/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(PROBLEM_JSON));

            verify(service, never()).getProductById(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("PUT should return 200 when the product is updated")
        void updateReturn200() throws Exception {
            // Arrange
            when(service.updateProduct(any(ProductRequestDto.class), eq(10L))).thenReturn(response(10L, "LOG-K380"));

            // Act & Assert
            mockMvc.perform(put(PATH + "/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.name").value("Keyboard K380"));
        }

        @Test
        @DisplayName("PUT should return 404 when the product does not exist")
        void updateReturn404() throws Exception {
            // Arrange
            when(service.updateProduct(any(ProductRequestDto.class), eq(99L)))
                    .thenThrow(CusEntityNotFoundException.of("Product", 99L));

            // Act & Assert
            mockMvc.perform(put(PATH + "/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value(containsString("99")));
        }

        @Test
        @DisplayName("PUT should return 409 when the sku differs from the stored one")
        void updateReturn409() throws Exception {
            // Arrange
            when(service.updateProduct(any(ProductRequestDto.class), eq(10L)))
                    .thenThrow(CusEntityConflictException.immutableField("Product", "sku", "OLD", "LOG-K380"));

            // Act & Assert
            mockMvc.perform(put(PATH + "/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value(containsString("sku")));
        }

        @Test
        @DisplayName("PUT should return 400 when the id is not a valid number")
        void updateReturn400() throws Exception {
            // Act & Assert
            mockMvc.perform(put(PATH + "/abc")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(PROBLEM_JSON));

            verify(service, never()).updateProduct(any(), any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("DELETE should return 204 when the product is deleted")
        void deleteReturn204() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(PATH + "/10"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(service).deleteProduct(10L);
        }

        @Test
        @DisplayName("DELETE should return 404 when the product does not exist")
        void deleteReturn404() throws Exception {
            // Arrange
            doThrow(CusEntityNotFoundException.of("Product", 99L))
                    .when(service).deleteProduct(99L);

            // Act & Assert
            mockMvc.perform(delete(PATH + "/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value(containsString("99")));
        }

        @Test
        @DisplayName("DELETE should return 400 when the id is not a valid number")
        void deleteReturn400() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(PATH + "/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(PROBLEM_JSON));

            verify(service, never()).deleteProduct(any());
        }
    }
}
