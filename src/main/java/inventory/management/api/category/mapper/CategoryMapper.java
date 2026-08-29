package inventory.management.api.category.mapper;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.category.dto.CategoryRequestDto;
import inventory.management.api.category.entity.CategoryEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {
    public CategoryDto toDto(CategoryEntity entity){
        CategoryDto dto = new CategoryDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );

        return dto;
    }

    public List<CategoryDto> toDtoAll(List<CategoryEntity> entities){
        return entities.stream().map(this::toDto).toList();
    }

    public CategoryEntity toEntity(CategoryRequestDto requestDto){
        CategoryEntity entity = new CategoryEntity();
        entity.setName(requestDto.name());
        entity.setDescription(requestDto.description());

        return entity;
    }
}
