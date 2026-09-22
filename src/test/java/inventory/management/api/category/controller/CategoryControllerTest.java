package inventory.management.api.category.controller;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.category.dto.CategoryRequestDto;
import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.service.CategoryService;
import inventory.management.api.exception.CusEntityAlreadyExistsException;
import inventory.management.api.exception.CusEntityConflictException;
import inventory.management.api.exception.CusEntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.core.TypeInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {
    private static final String PATH = "/api/v1";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("create")
    class Create {
        private final CategoryRequestDto request = new CategoryRequestDto("ACTION", "Action.");

        @Test
        @DisplayName("POST should return 201 with Location header")
        void createReturn201() throws Exception {
            // Arrange
            CategoryDto response = new CategoryDto(1L, "ACTION", "Action.");

            when(service.createCategory(any(CategoryRequestDto.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post(PATH + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/categories/1")))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("ACTION"));

        }

        @Test
        @DisplayName("POST should return 409 when category exists")
        void createReturn409() throws Exception {
            // Arrange
            when(service.createCategory(any(CategoryRequestDto.class)))
                    .thenThrow(CusEntityAlreadyExistsException.of("Category", "name", "ACTION"));

            // Act & Assert
            mockMvc.perform(post(PATH + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.detail").value(containsString("ACTION")));
        }

        @Test
        @DisplayName("POST should return 400 when Invalid data")
        void createReturn400() throws Exception {
            // Act & Assert
            mockMvc.perform(post(PATH + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"   \"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("name"))
                    .andExpect(jsonPath("$.errors[0].code").value("NotBlank"));
        }

        @Test
        @DisplayName("POST should return 400 when the description is longer than its column")
        void createReturn400DescriptionTooLong() throws Exception {
            // Arrange
            CategoryRequestDto tooLong = new CategoryRequestDto("ACTION", "x".repeat(501));

            // Act & Assert
            mockMvc.perform(post(PATH + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tooLong)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("description"))
                    .andExpect(jsonPath("$.errors[0].code").value("Size"));

            verify(service, never()).createCategory(any());
        }

        @Test
        @DisplayName("POST should return 400 when the JSON is malformed")
        void createReturn400MalformedJson() throws Exception {
            // Act & Assert
            mockMvc.perform(post(PATH + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"ACTION\", "))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("application/problem+json"));

            verify(service, never()).createCategory(any());
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {
        @Test
        @DisplayName("GET should return 200 with the page content and its metadata")
        void getAllReturn200() throws Exception {
            // Arrange
            List<CategoryDto> categoryDtos = List.of(
                    new CategoryDto(1L, "ACTION", "Action."),
                    new CategoryDto(2L, "ANIMATED", "Animated")
            );

            when(service.getAllCategories(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(categoryDtos, PageRequest.of(0, 20), 2));

            // Act & Assert
            mockMvc.perform(get(PATH + "/categories"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[1].name").value("ANIMATED"))
                    .andExpect(jsonPath("$.page.totalElements").value(2))
                    .andExpect(jsonPath("$.page.number").value(0));
        }

        @Test
        @DisplayName("GET without params should ask for page 0, size 20, ordered by id")
        void getAllDefaultPageable() throws Exception {
            // Arrange
            when(service.getAllCategories(any(Pageable.class))).thenReturn(Page.empty());

            // Act
            mockMvc.perform(get(PATH + "/categories")).andExpect(status().isOk());

            // Assert
            verify(service).getAllCategories(PageRequest.of(0, 20, Sort.by("id")));
        }

        @Test
        @DisplayName("GET should pass page, size and sort through, capping size at 100")
        void getAllCustomPageable() throws Exception {
            // Arrange
            when(service.getAllCategories(any(Pageable.class))).thenReturn(Page.empty());

            // Act
            mockMvc.perform(get(PATH + "/categories?page=1&size=1000&sort=name,desc"))
                    .andExpect(status().isOk());

            // Assert
            verify(service).getAllCategories(PageRequest.of(1, 100, Sort.by(Sort.Direction.DESC, "name")));
        }

        @Test
        @DisplayName("GET should return 400 when sort names an unknown property")
        void getAllUnknownSort400() throws Exception {
            // Arrange
            when(service.getAllCategories(any(Pageable.class))).thenThrow(
                    new PropertyReferenceException("nombre", TypeInformation.of(CategoryEntity.class), List.of()));

            // Act & Assert
            mockMvc.perform(get(PATH + "/categories?sort=nombre"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.detail").value(containsString("nombre")));
        }

        @Test
        @DisplayName("PATCH on the collection should return 405")
        void patchReturn405() throws Exception {
            // Act & Assert
            mockMvc.perform(patch(PATH + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"ACTION\",\"description\":\"Action\"}"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(content().contentType("application/problem+json"));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("GET /{id} should return 200 with the category")
        void getByIdReturn200() throws Exception {
            // Arrange
            when(service.getCategoryById(1L)).thenReturn(new CategoryDto(1L, "ACTION", "Action."));

            // Act & Assert
            mockMvc.perform(get(PATH + "/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("ACTION"));
        }

        @Test
        @DisplayName("GET /{id} should return 404 when the category does not exist")
        void getByIdReturn404() throws Exception {
            // Arrange
            when(service.getCategoryById(99L)).thenThrow(CusEntityNotFoundException.of("Category", 99L));

            // Act & Assert
            mockMvc.perform(get(PATH + "/categories/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.detail").value(containsString("99")));
        }

        @Test
        @DisplayName("GET /{id} should return 400 when the id is not a valid number")
        void getByIdReturn400() throws Exception {
            // Act & Assert
            mockMvc.perform(get(PATH + "/categories/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("application/problem+json"));

            verify(service, never()).getCategoryById(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("PUT should return 200 when the category is updated")
        void updateReturn200() throws Exception {
            // Arrange
            CategoryRequestDto requestDto = new CategoryRequestDto("ACTION", "Action");
            CategoryDto response = new CategoryDto(1L, "ACTION", "Action");

            when(service.updateCategory(any(CategoryRequestDto.class), eq(1L))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(put(PATH + "/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("ACTION"));
        }

        @Test
        @DisplayName("PUT should return 404 when the category does not exist")
        void updateReturn404() throws Exception {
            // Arrange: el service no encuentra el id; el advice traduce a 404.
            CategoryRequestDto requestDto = new CategoryRequestDto("ACTION", "Action");

            when(service.updateCategory(any(CategoryRequestDto.class), eq(99L)))
                    .thenThrow(CusEntityNotFoundException.of("Category", 99L));

            // Act & Assert
            mockMvc.perform(put(PATH + "/categories/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.detail").value(containsString("99")));
        }

        @Test
        @DisplayName("PUT should return 400 when the id is not a valid number")
        void updateReturn400() throws Exception {
            // Act & Assert: Spring falla al convertir "abc" a Long y nunca llega al service.
            mockMvc.perform(put(PATH + "/categories/abc")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"ACTION\",\"description\":\"Action\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("application/problem+json"));

            verify(service, never()).updateCategory(any(), any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("DELETE should return 204 when the category is deleted")
        void deleteReturn204() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(PATH + "/categories/1"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(service).deleteCategory(1L);
        }

        @Test
        @DisplayName("DELETE should return 404 when the category does not exist")
        void deleteReturn404() throws Exception {
            // Arrange
            doThrow(CusEntityNotFoundException.of("Category", 99L))
                    .when(service).deleteCategory(99L);

            // Act & Assert
            mockMvc.perform(delete(PATH + "/categories/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.detail").value(containsString("99")));
        }

        @Test
        @DisplayName("DELETE should return 409 when the category still has products")
        void deleteReturn409() throws Exception {
            // Arrange
            doThrow(CusEntityConflictException.hasDependents("Category", 1L, "products"))
                    .when(service).deleteCategory(1L);

            // Act & Assert
            mockMvc.perform(delete(PATH + "/categories/1"))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.detail").value(containsString("products")));
        }

        @Test
        @DisplayName("DELETE should return 400 when the id is not a valid number")
        void deleteReturn400() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(PATH + "/categories/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("application/problem+json"));

            verify(service, never()).deleteCategory(any());
        }
    }

}