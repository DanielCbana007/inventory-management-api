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
    private static final String ENTITY_NAME = "Category";

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Transactional
    public CategoryDto createCategory(CategoryRequestDto requestDto) {

        // la comprobacion explicita da un 409 con mensaje util
        //     ("Category with name 'X' already exists") en vez del generico que llegaba
        //     desde la restriccion unique de Postgres. Esa restriccion NO sobra: es la
        //     garantia real para la condicion de carrera entre este if y el save().
        if (categoryRepository.existsByName(requestDto.name())){
            throw CusEntityAlreadyExistsException.of(ENTITY_NAME, requestDto.name());
        }

        CategoryEntity newCategory = this.mapper.toEntity(requestDto);
        return  this.mapper.toDto(this.categoryRepository.save(newCategory));
    }

    // MEJORA [§2.4]: findAll() sin Pageable trae la tabla entera. Ver la nota del controller.
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        List<CategoryEntity> listCategories = this.categoryRepository.findAll();
        return this.mapper.toDtoAll(listCategories);
    }

    @Transactional
    public CategoryDto updateCategory(CategoryRequestDto requestDto, Long id) {
        CategoryEntity category = this.categoryRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of(ENTITY_NAME, id));

        // BLOQUEANTE de la auditoria 3 cerrado. Estas dos lineas estuvieron en
        //     orden inverso y el PUT respondia los valores ANTIGUOS mientras guardaba los
        //     nuevos (verificado: la respuesta decia "antes" y la base "DESPUES").
        //     El orden importa porque el record es inmutable: toDto congela el estado.
        //     Cubierto por CategoryServiceTest.updateReturnsNewValues, que solo detecta
        //     el fallo porque el test usa el mapper REAL y no un mock.
        category.updateWith(requestDto.name(), requestDto.description());
        return this.mapper.toDto(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        CategoryEntity category = this.categoryRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of(ENTITY_NAME, id));

        this.categoryRepository.delete(category);
    }

    // sin save() explicito en updateCategory. Dentro de la transaccion, Hibernate
    //     detecta el cambio por dirty checking y emite el UPDATE al confirmar. Depende
    //     por completo de que exista @Transactional: si alguien la quita, deja de guardar
    //     en silencio. Solo un test de integracion (Testcontainers, Proyecto 2) lo cubre.
}
