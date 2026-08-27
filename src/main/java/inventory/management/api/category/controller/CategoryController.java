package inventory.management.api.category.controller;

import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.services.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // CRUD --> Create
    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody CategoryEntity category){
        try {
            this.categoryService.createCategory(category);
            return ResponseEntity.ok("Create category");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // CRUD --> Read
    @GetMapping("/categories")
    public List<CategoryEntity> getAll(){
        return this.categoryService.getAllCategories();
    }

    // CRUD --> Update
    @PostMapping("/update/{nameCategory}")
    public ResponseEntity<String> update(@RequestBody CategoryEntity category, @PathVariable String nameCategory){
        try {
            this.categoryService.updateCategory(category, nameCategory);
            return ResponseEntity.ok("Update category");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // CRUD --> Delete
    @PostMapping("/delete/{nameCategory}")
    public ResponseEntity<String> delete(@PathVariable String nameCategory){
        try {
            this.categoryService.aleteCategory(nameCategory);
            return ResponseEntity.ok("Delete category");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
