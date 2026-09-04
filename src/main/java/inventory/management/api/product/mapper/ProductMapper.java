package inventory.management.api.product.mapper;


import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.mapper.CategoryMapper;
import inventory.management.api.product.dto.ProductDto;
import inventory.management.api.product.dto.ProductRequestDto;
import inventory.management.api.product.entity.ProductEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {
    private final CategoryMapper mapper;

    public ProductMapper(CategoryMapper mapper) {
        this.mapper = mapper;
    }

    public ProductDto toDto(ProductEntity entity) {

        return new ProductDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSku(),
                entity.getPrice(),
                entity.getStock(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                mapper.toDto(entity.getCategory())
        );
    }

    public List<ProductDto> toDtoAll(List<ProductEntity> entities) {

        return entities.stream().map(this::toDto).toList();
    }

    public ProductEntity toEntity(ProductRequestDto requestDto, CategoryEntity category) {
        return new ProductEntity(
                requestDto.name(),
                requestDto.description(),
                requestDto.sku(),
                requestDto.price(),
                requestDto.stock(),
                category
        );
    }
}
