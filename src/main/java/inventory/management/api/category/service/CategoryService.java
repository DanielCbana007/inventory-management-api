package inventory.management.api.category.service;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.category.dto.CategoryRequestDto;
import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.mapper.CategoryMapper;
import inventory.management.api.category.repository.CategoryRepository;
import inventory.management.api.exception.CusEntityAlreadyExistsException;
import inventory.management.api.exception.CusEntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Transactional
    public CategoryDto createCategory(CategoryRequestDto requestDto) {

        if (categoryRepository.existsByName(requestDto.name())){
            throw CusEntityAlreadyExistsException.of("Category", requestDto.name());
        }

        CategoryEntity newCategory = this.mapper.toEntity(requestDto);
        CategoryDto dto = this.mapper.toDto(this.categoryRepository.save(newCategory));

        return dto;
    }

    // TODO [§2.4]: acepta un Pageable y devuelve Page<CategoryDto> usando findAll(pageable).
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        List<CategoryEntity> listCategories = this.categoryRepository.findAll();
        return this.mapper.toDtoAll(listCategories);
    }

    @Transactional
    public CategoryDto updateCategory(CategoryRequestDto requestDto, Long id) {
        CategoryEntity category = this.categoryRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of("Category", id));

        CategoryDto dto = this.mapper.toDto(category);
        category.updateWith(requestDto.name(), requestDto.description());
        return dto;
    }

    @Transactional
    public void deleteCategory(Long id) {
        CategoryEntity category = this.categoryRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of("Category", id));

        this.categoryRepository.delete(category);
    }
}
