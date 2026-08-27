package inventory.management.api.category.services;

import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // CRUD --> Create --> Read --> Update --> Delete

    // CRUD --> Create
    public void createCategory(CategoryEntity category){
        String name = category.getName();
        String description = category.getDescription();

        CategoryEntity newCategory = new CategoryEntity(name, description);

        this.categoryRepository.save(category);
    }

    // CRUD --> Read
    public List<CategoryEntity> getAllCategories(){
        return this.categoryRepository.findAll();
    }

    // CRUD --> Update
    public void updateCategory(CategoryEntity category, String nameCategory){
        String name = category.getName();
        String description = category.getDescription();

        if (this.categoryRepository.existsByName(nameCategory)){
            CategoryEntity byName = this.categoryRepository.findByName(nameCategory);

            byName.setName(name);
            byName.setDescription(description);
            this.categoryRepository.save(byName);
        }
    }

    // CRUD --> Delete
    public void aleteCategory(String name){
        CategoryEntity category = this.categoryRepository.findByName(name);

        this.categoryRepository.delete(category);
    }
}
