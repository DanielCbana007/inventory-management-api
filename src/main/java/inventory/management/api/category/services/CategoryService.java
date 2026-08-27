package inventory.management.api.category.services;

import inventory.management.api.category.entity.CategoryEntity;
import inventory.management.api.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// MEJORA [§3.5]: paquete "services" en plural, pero controller/entity/repository
//                van en singular. Inconsistencia dentro del mismo módulo.
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    // OK: inyección por constructor con campo final.
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ERROR [§1.1]: toda la clase habla en CategoryEntity. La entidad no debería
    //               salir del service: hacia afuera van DTOs.
    // FALTA [§1.4g]: ningún método de escritura declara @Transactional.

    // CRUD --> Create --> Read --> Update --> Delete

    // CRUD --> Create
    public void createCategory(CategoryEntity category){
        String name = category.getName();
        String description = category.getDescription();

        // BUG [§1.4a]: newCategory se construye y se descarta. Es código muerto...
        CategoryEntity newCategory = new CategoryEntity(name, description);

        // BUG [§1.4a]: ...y aquí guardas "category", el objeto que vino del request.
        // SEGURIDAD [§1.1]: si el JSON traía "id", Hibernate hace UPDATE en vez de
        //               INSERT: el POST SOBRESCRIBE una categoría existente.
        //               Ironía: la línea muerta de arriba era la defensa correcta.
        this.categoryRepository.save(category);

        // FALTA [§1.3]: no devuelves nada, así que el controller no puede responder
        //               201 con el id del recurso creado.
        // FALTA: nadie comprueba si el name ya existe. La restricción unique de la
        //        entidad lo detecta en la base y sale como 500 en vez de 409 Conflict.
    }

    // CRUD --> Read
    public List<CategoryEntity> getAllCategories(){
        // MEJORA [§3.2]: findAll() trae la tabla entera a memoria. Sin paginación.
        return this.categoryRepository.findAll();
    }

    // CRUD --> Update
    public void updateCategory(CategoryEntity category, String nameCategory){
        String name = category.getName();
        String description = category.getDescription();

        // MEJORA [§3.3]: existsByName + findByName consultan lo mismo dos veces.
        //                Dos viajes a Neon donde bastaba uno. Code smell visible.
        if (this.categoryRepository.existsByName(nameCategory)){
            CategoryEntity byName = this.categoryRepository.findByName(nameCategory);

            // MEJORA [§3.1]: modelo anémico. Modificas la entidad desde fuera con
            //                setters en vez de pedirle a ella que se actualice.
            //                Es lo contrario del GRASP "Experto en Información".
            byName.setName(name);
            byName.setDescription(description);
            this.categoryRepository.save(byName);
        }
        // BUG [§1.4b]: FALLO SILENCIOSO. Si el if es falso, el método termina sin
        //              hacer nada y sin avisar. El controller responde 200 OK.
        //              Un fallo silencioso es peor que una excepción: el cliente
        //              construye lógica sobre una respuesta que miente.
    }

    // CRUD --> Delete
    // BUG [§1.4e]: typo, debería ser deleteCategory. Trivial de arreglar y por eso
    //              mismo caro: en revisión de código se lee como que nadie releyó.
    public void aleteCategory(String name){
        // BUG [§1.4d]: findByName devuelve null si no existe (ver el repositorio)...
        CategoryEntity category = this.categoryRepository.findByName(name);

        // BUG [§1.4d]: ...y delete(null) lanza InvalidDataAccessApiUsageException.
        //              El cliente recibe 500 ("el servidor está roto") donde tocaba
        //              404 ("pediste algo que no existe"). Confundirlos hace que el
        //              cliente reintente algo que nunca va a funcionar.
        this.categoryRepository.delete(category);
    }
}
