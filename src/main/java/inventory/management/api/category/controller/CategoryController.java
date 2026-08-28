package inventory.management.api.category.controller;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.category.dto.CategoryRequestDto;
import inventory.management.api.category.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/*
 * ==== LEYENDA DE LAS ANOTACIONES (auditoria-1.md) ====
 * SEGURIDAD : agujero explotable. Falla inmediata en la rúbrica.
 * BUG       : el código hace algo incorrecto al ejecutarse.
 * ERROR     : viola un estándar del roadmap (capas, REST, errores).
 * FALTA     : algo que debería existir y no está.
 * MEJORA    : no bloquea, pero separa Habilita de Domina.
 * El sufijo [§x.y] apunta a la sección de docs/seguimiento/auditoria-1.md
 */

// ERROR [§1.3]: falta @RequestMapping("/api/v1/categories"). Las rutas cuelgan de
//               la raíz, sin agrupar por recurso y sin versionado.
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    // OK: inyección por constructor con campo final. Es la forma correcta (IoC/DI).
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // CRUD --> Create
    // ERROR [§1.3]: el verbo va en el método HTTP, no en la URL. "/create" sobra.
    @PostMapping("")
    // ERROR [§1.3]: devuelve 200 OK; una creación debe responder 201 + Location.
    // ERROR [§1.3]: el cuerpo es un String suelto. El cliente no puede parsearlo
    //               ni sabe qué id se le asignó a la categoría que acaba de crear.
    // SEGURIDAD [§1.1]: @RequestBody sobre la entidad JPA = mass assignment. El
    //               cliente puede mandar "id" y escribir la clave primaria.
    // FALTA [§1.5]: sin @Valid. Un name vacío llega hasta Postgres y vuelve como 500.
    public ResponseEntity<CategoryDto> create(@RequestBody @Valid CategoryRequestDto requestDto) {
        // ERROR [§1.2]: try/catch disperso. El estándar pide @RestControllerAdvice.
        CategoryDto created = this.categoryService.createCategory(requestDto);
        URI location = URI.create("/api/v1/categories/" + created.id());

        return ResponseEntity.created(location).body(created);
        // ERROR [§1.2]: capturas para relanzar envuelto. No recuperas, no traduces
        //               a status code, y ocultas el tipo original. Todo acaba en 500.

    }

    // CRUD --> Read
    @GetMapping("")
    // ERROR [§1.1]: devuelves la entidad JPA. Tu contrato público ES tu tabla:
    //               renombrar una columna rompe a todos los clientes.
    // MEJORA [§3.2]: sin paginación. Pregunta de entrevista: ¿y con 500k filas?
    public List<CategoryDto> getAll() {
        List<CategoryDto> body = this.categoryService.getAllCategories();

        return body;
    }

    // CRUD --> Update
    // ERROR [§1.3]: un update no es POST. Aquí se decide PUT (reemplazo total) o
    //               PATCH (parcial). El roadmap evalúa esa distinción directamente.
    // ERROR [§1.3]: identificas el recurso por "name", que es mutable. La URL de
    //               una categoría cambia cuando la editas; un id debe ser estable.
    @PutMapping("/{id}")
    // BUG [§1.4b]: si la categoría no existe devuelve 200 OK sin haber hecho nada.
    //              Le mientes al cliente sobre el resultado.
    public ResponseEntity<CategoryDto> update(@RequestBody @Valid CategoryRequestDto requestDto, @PathVariable Long id) {
        CategoryDto body = this.categoryService.updateCategory(requestDto, id);

        return ResponseEntity.ok(body);
    }

    // CRUD --> Delete
    // ERROR [§1.3]: debe ser @DeleteMapping. POST es el único verbo que HTTP define
    //               como NO idempotente: declaras lo contrario de la garantía real.
    @DeleteMapping("/{id}")
    // BUG [§1.4d]: si no existe, el service revienta con 500 en vez de responder 404.
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
        // ERROR [§1.3]: un delete sin cuerpo útil debería ser 204 No Content.
    }

    // FALTA [§2.7]: ninguna anotación OpenAPI (@Tag, @Operation, @ApiResponse).
    //               springdoc está publicando CategoryEntity como esquema: hoy
    //               documentas tu tabla, no tu contrato.
}
