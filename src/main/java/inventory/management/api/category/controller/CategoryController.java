package inventory.management.api.category.controller;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.category.dto.CategoryRequestDto;
import inventory.management.api.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Marcas de revision. Apuntan a las notas de la revision 5; el sufijo [§x] es la
 * seccion donde esta el porque largo.
 *
 *   BLOQUEANTE  impide cerrar la revision. Maxima prioridad.
 *   ERROR       comportamiento incorrecto en el codigo que ya existe.
 *   FALTA       alcance previsto que aun no se ha abordado.
 *   MEJORA      no bloquea; es lo que separa Competente de Experto en la escala.
 *   OK          esta bien hecho y se defiende en entrevista. No lo toques.
 *
 * Regla: cada marca se borra en el MISMO commit que resuelve lo que describe. Una marca
 * que describe un defecto ya corregido miente, y quien lea el archivo se la cree.
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Create, read, replace and delete inventory categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(
            summary = "Create category",
            description = "Registers a new category and returns the created resource with the id assigned by the database. The Location header points to its URL. The name must be unique.",
            responses = {
                    // MEJORA [§3.4]: el 201 devuelve una cabecera Location y aqui no se declara.
                    //         Un generador de clientes lee el contrato, no la prosa de la
                    //         descripcion, asi que el cliente generado la ignora.
                    @ApiResponse(responseCode = "201", description = "Category created",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CategoryDto.class))),
                    @ApiResponse(responseCode = "409", description = "A category with that name already exists",
                            content = @Content(mediaType = "application/problem+json",
                                    schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "400", description = "The name is missing, blank or longer than 100 characters",
                            content = @Content(mediaType = "application/problem+json",
                                    schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public ResponseEntity<CategoryDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Specify how you want to create the category")
            @RequestBody @Valid CategoryRequestDto requestDto) {
        CategoryDto created = this.categoryService.createCategory(requestDto);
        // ERROR [§1.1]: esta Location apunta a una URL que responde 405, porque abajo no hay
        //        ningun GET /{id}. Verificado en la revision 5:
        //            POST /api/v1/categories      -> 201  Location: .../categories/276
        //            GET  /api/v1/categories/276  -> 405  "Method 'GET' is not supported."
        //        RFC 9110 10.2.2: Location es la URI del recurso creado. El cliente que la
        //        siga -que es justo para lo que esta- se rompe.
        //        Resuelto cuando: seguir la Location de un POST recien hecho devuelve 200
        //        con el recurso.
        // Se construye desde la peticion actual: evita duplicar la ruta y las URLs mal
        // formadas al concatenar a mano.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(
            summary = "Get all categories",
            description = "Returns the whole catalogue. Not paginated yet.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of categories",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = CategoryDto.class)))),
            }
    )
    // MEJORA [§2.3]: sin paginacion; devuelve la tabla entera. Cambia el contrato de
    //         List<T> a Page<T>, asi que va DESPUES de los tests: hacerlo antes obliga a
    //         reescribirlos.
    public List<CategoryDto> getAll() {
        return this.categoryService.getAllCategories();
    }

    // FALTA [§2.2]: no existe GET /{id}. Sin el, el recurso de item solo tiene 3 de los 4
    //        verbos y la Location del POST no se puede seguir. Es requisito de nivel junior
    //        evaluada directamente. Debe responder 200 con el recurso y 404 con
    //        ProblemDetail, y llevar su @Operation como los demas.

    @PutMapping("/{id}")
    @Operation(
            summary = "Update category by ID",
            description = "Replaces the category as a whole. This is a PUT, not a PATCH: fields you do not send are set to null, they do not keep their previous value.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CategoryDto.class))),
                    @ApiResponse(responseCode = "400", description = "The name is invalid or the id is not a number",
                            content = @Content(mediaType = "application/problem+json",
                                    schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "404", description = "No category exists with that id",
                            content = @Content(mediaType = "application/problem+json",
                                    schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public ResponseEntity<CategoryDto> update(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Specify which values you want to update.")
            @RequestBody @Valid CategoryRequestDto requestDto,
            @Parameter(
                    name = "id",
                    description = "Id of the category to replace",
                    example = "3",
                    required = true
            )
            @PathVariable Long id) {
        CategoryDto body = this.categoryService.updateCategory(requestDto, id);

        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete category",
            description = "Deletes the given category. Returns no body.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Category deleted",
                            content = @Content),
                    @ApiResponse(responseCode = "400", description = "The id is not a valid number",
                            content = @Content(mediaType = "application/problem+json",
                                    schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "404", description = "No category exists with that id",
                            content = @Content(mediaType = "application/problem+json",
                                    schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public ResponseEntity<Void> delete(
            @Parameter(
                    name = "id",
                    description = "Id of the category to delete",
                    example = "3",
                    required = true
            )
            @PathVariable Long id
    ) {
        this.categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }
}
