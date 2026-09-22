package inventory.management.api.product.service;

import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.repository.CategoryRepository;
import inventory.management.api.exception.CusEntityAlreadyExistsException;
import inventory.management.api.exception.CusEntityConflictException;
import inventory.management.api.exception.CusEntityNotFoundException;
import inventory.management.api.product.repository.ProductRepository;
import inventory.management.api.product.dto.ProductDto;
import inventory.management.api.product.dto.ProductRequestDto;
import inventory.management.api.product.entity.ProductEntity;
import inventory.management.api.product.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductMapper mapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    // Create
    @Transactional
    public ProductDto createProduct(ProductRequestDto requestDto){
        if (this.productRepository.existsBySku(requestDto.sku())){
            throw CusEntityAlreadyExistsException.of("Product", "sku", requestDto.sku());
        }

        // OK [§4]: la categoria se RESUELVE por id contra la base, no se acepta del cliente.
        //     Aceptar aqui un CategoryDto completo seria mass assignment sobre la tabla de
        //     categorias desde el endpoint de productos. Esto se defiende solo en entrevista.
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

    // readOnly no es solo estilo: category es LAZY y open-in-view=false, asi que el toDto
    // tiene que correr dentro de la transaccion o getCategory().getName() lanza
    // LazyInitializationException.
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id){
        ProductEntity product = this.productRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of("Product", id));
        return this.mapper.toDto(product);
    }

    // Update
    @Transactional
    public ProductDto updateProduct(ProductRequestDto requestDto, Long id){
        ProductEntity product = this.productRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of("Product", id));
        if (!product.getSku().equals(requestDto.sku())) {
            throw CusEntityConflictException.immutableField("Product", "sku", product.getSku(), requestDto.sku());
        }
        CategoryEntity category = this.categoryRepository.findById(requestDto.categoryId())
                .orElseThrow(() -> CusEntityNotFoundException.of("Category", requestDto.categoryId()));

        product.updateWith(
                requestDto.name(),
                requestDto.description(),
                requestDto.price(),
                requestDto.stock(),
                category
        );

        this.productRepository.flush();

        return this.mapper.toDto(product);
    }

    // Delete
    // OK [§4]: las tres escrituras con @Transactional y la lectura con readOnly = true, y
    //     updateProduct se apoya en el dirty checking sin llamar a save(). Saber que ese save
    //     sobra dentro de una transaccion es lo que separa Competente de Inicial en este eje.
    @Transactional
    public void deleteProduct(Long id){
        ProductEntity product = this.productRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of("Product", id));

        this.productRepository.delete(product);
    }
}
