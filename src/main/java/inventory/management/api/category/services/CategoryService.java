package inventory.management.api.category.services;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.category.dto.CategoryRequestDto;
import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.mapper.CategoryMapper;
import inventory.management.api.category.repository.CategoryRepository;
import inventory.management.api.exception.CusEntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// MEJORA [§3.5]: paquete "services" en plural, pero controller/entity/repository
//                van en singular. Inconsistencia dentro del mismo módulo.
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    // OK: inyección por constructor con campo final.

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }


    // ERROR [§1.1]: toda la clase habla en CategoryEntity. La entidad no debería
    //               salir del service: hacia afuera van DTOs.

    // CRUD --> Create --> Read --> Update --> Delete

    // CRUD --> Create
    public CategoryDto createCategory(CategoryRequestDto requestDto) {
        // BUG [§1.4a]: newCategory se construye y se descarta. Es código muerto...
        CategoryEntity newCategory = this.mapper.toEntity(requestDto);


        // BUG [§1.4a]: ...y aquí guardas "category", el objeto que vino del request.
        // SEGURIDAD [§1.1]: si el JSON traía "id", Hibernate hace UPDATE en vez de
        //               INSERT: el POST SOBRESCRIBE una categoría existente.
        //               Ironía: la línea muerta de arriba era la defensa correcta.
        CategoryDto dto = this.mapper.toDto(this.categoryRepository.save(newCategory));
        return dto;

        // FALTA [§1.3]: no devuelves nada, así que el controller no puede responder
        //               201 con el id del recurso creado.
        // FALTA: nadie comprueba si el name ya existe. La restricción unique de la
        //        entidad lo detecta en la base y sale como 500 en vez de 409 Conflict.
    }

    // CRUD --> Read
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        // MEJORA [§3.2]: findAll() trae la tabla entera a memoria. Sin paginación.
        List<CategoryEntity> listCategories = this.categoryRepository.findAll();
        return this.mapper.toDtoAll(listCategories);
    }

    // CRUD --> Update
    @Transactional
    public CategoryDto updateCategory(CategoryRequestDto requestDto, Long id) {

        String name = requestDto.name();
        String description = requestDto.description();

        CategoryEntity category = this.categoryRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of("Category", id));

        category.setName(name);
        category.setDescription(description);
        CategoryDto dto = this.mapper.toDto(this.categoryRepository.save(category));
        return dto;


        // MEJORA [§3.3]: existsByName + findByName consultan lo mismo dos veces.
        //                Dos viajes a Neon donde bastaba uno. Code smell visible.

        // MEJORA [§3.1]: modelo anémico. Modificas la entidad desde fuera con
        //                setters en vez de pedirle a ella que se actualice.
        //                Es lo contrario del GRASP "Experto en Información".
        // BUG [§1.4b]: FALLO SILENCIOSO. Si el if es falso, el método termina sin
        //              hacer nada y sin avisar. El controller responde 200 OK.
        //              Un fallo silencioso es peor que una excepción: el cliente
        //              construye lógica sobre una respuesta que miente.
    }

    // CRUD --> Delete
    // BUG [§1.4e]: typo, debería ser deleteCategory. Trivial de arreglar y por eso
    //              mismo caro: en revisión de código se lee como que nadie releyó.
    @Transactional
    public void deleteCategory(Long id) {
        // BUG [§1.4d]: findByName devuelve null si no existe (ver el repositorio)...
        CategoryEntity category = this.categoryRepository.findById(id)
                .orElseThrow(() -> CusEntityNotFoundException.of("Category", id));

        // BUG [§1.4d]: ...y delete(null) lanza InvalidDataAccessApiUsageException.
        //              El cliente recibe 500 ("el servidor está roto") donde tocaba
        //              404 ("pediste algo que no existe"). Confundirlos hace que el
        //              cliente reintente algo que nunca va a funcionar.
        this.categoryRepository.delete(category);
    }
}
