package inventory.management.api.category.service;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.category.dto.CategoryRequestDto;
import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.mapper.CategoryMapper;
import inventory.management.api.category.repository.CategoryRepository;
import inventory.management.api.exception.CusEntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// MEJORA: renombra el paquete a "service", en singular como controller/entity/repository.
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    // TODO [§1.1]: comprueba el duplicado antes de guardar:
    //     if (categoryRepository.existsByName(requestDto.name()))
    //         throw CusEntityAlreadyExistsException.of("Category", requestDto.name());
    //
    //   Hoy el 409 llega por la restricción unique de Postgres, así que el mensaje es
    //   genérico y no dice qué nombre chocó. Deja esa restricción como red de seguridad:
    //   entre tu comprobación y el save() cabe otra petición con el mismo nombre.
    //
    //   Al añadirlo serán DOS llamadas al repositorio, así que este método pasará a
    //   necesitar @Transactional. Hoy no lo lleva y es correcto: una sola llamada, y
    //   save() ya es transaccional por dentro.
    public CategoryDto createCategory(CategoryRequestDto requestDto) {
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

        String name = requestDto.name();
        String description = requestDto.description();

        CategoryEntity category = this.categoryRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of("Category", id));

        // MEJORA: mueve esto a un método de la entidad, p.ej. category.updateWith(name, description).
        //         Cambiarla desde fuera con setters es un modelo anémico: la responsabilidad de
        //         modificar un dato pertenece a quien lo tiene (GRASP Experto en Información).
        category.setName(name);
        category.setDescription(description);

        // MEJORA: este save() se puede borrar. Dentro de @Transactional, Hibernate detecta el
        //         cambio por dirty checking y emite el UPDATE al confirmar.
        CategoryDto dto = this.mapper.toDto(this.categoryRepository.save(category));
        return dto;
    }

    @Transactional
    public void deleteCategory(Long id) {
        CategoryEntity category = this.categoryRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of("Category", id));

        this.categoryRepository.delete(category);
    }

    // TODO [§2.1]: crea CategoryServiceTest con JUnit 5 + Mockito, patrón AAA, con @Mock
    //              del repositorio y del mapper. Casos mínimos:
    //                - createCategory devuelve el DTO con el id que asignó save()
    //                - updateCategory lanza CusEntityNotFoundException si el id no existe
    //                - updateCategory persiste los campos nuevos
    //                - deleteCategory lanza CusEntityNotFoundException si el id no existe
    //              Es el único eje de la rúbrica que sigue sin poder evaluarse.
}
