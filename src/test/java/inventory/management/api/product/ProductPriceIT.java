package inventory.management.api.product;

import inventory.management.api.IntegrationTestBase;
import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.repository.CategoryRepository;
import inventory.management.api.product.dto.ProductRequestDto;
import inventory.management.api.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
class ProductPriceIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanDatabase(){
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("POST should return 400 and persist nothing when the price has three decimals")
    void createReturns400WhenPriceHasThreeDecimals() throws Exception{
        // Arrange
        CategoryEntity category = categoryRepository.save(new CategoryEntity("ELECTRONIC", "For you projects."));
        ProductRequestDto threeDecimals = new ProductRequestDto("Keyboard K380", "Bluetooth keyboard",
                "LOG-K380", new BigDecimal("1.999"), 120, category.getId());
        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(threeDecimals)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("price"))
                .andExpect(jsonPath("$.errors[0].code").value("Digits"));
        assertThat(productRepository.count()).isZero();
    }

}
