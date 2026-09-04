package inventory.management.api.product.service;

import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.repository.CategoryRepository;
import inventory.management.api.exception.CusEntityAlreadyExistsException;
import inventory.management.api.exception.CusEntityNotFoundException;
import inventory.management.api.product.Repository.ProductRepository;
import inventory.management.api.product.dto.ProductDto;
import inventory.management.api.product.dto.ProductRequestDto;
import inventory.management.api.product.entity.ProductEntity;
import inventory.management.api.product.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdurctService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;

    public ProdurctService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductMapper mapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    // Create
    @Transactional
    public ProductDto createProduct(ProductRequestDto requestDto){
        if (this.productRepository.existsBySku(requestDto.sku())){
            throw CusEntityAlreadyExistsException.of("Product", requestDto.sku());
        }

        CategoryEntity category = categoryRepository.findById(requestDto.categoryId())
                .orElseThrow(() -> CusEntityNotFoundException.of("Category", requestDto.categoryId()));

        ProductEntity entity = mapper.toEntity(requestDto, category);
        return mapper.toDto(this.productRepository.save(entity));
    }

    // Read
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts(){
        List<ProductEntity> listProduct = this.productRepository.findAll();
        return this.mapper.toDtoAll(listProduct);
    }

    // Update
    @Transactional
    public ProductDto updateProduct(ProductRequestDto requestDto, Long id){
        ProductEntity product = this.productRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of("Product", id));
        CategoryEntity category = this.categoryRepository.findById(requestDto.categoryId())
                .orElseThrow(() -> CusEntityNotFoundException.of("Category", requestDto.categoryId()));

        product.updateWith(
                requestDto.name(),
                requestDto.description(),
                requestDto.price(),
                requestDto.stock(),
                category
        );

        return this.mapper.toDto(product);
    }

    // Delete
    @Transactional
    public void deleteProduct(Long id){
        ProductEntity product = this.productRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of("Product", id));

        this.productRepository.delete(product);
    }
}
