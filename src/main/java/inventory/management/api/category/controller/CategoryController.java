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

/*
 * ==== PENDIENTE - docs/seguimiento/auditoria-3.md ====
 *
 * ERROR  : comportamiento incorrecto en el codigo que existe.
 * FALTA  : alcance previsto que aun no se ha abordado.
 * MEJORA : no bloquea; separa Habilita de Domina.
 * OK     : esta bien hecho y es defendible en entrevista. No lo toques.
 *
 * El sufijo [§x] apunta a la seccion de docs/seguimiento/auditoria-3.md
 * Borra cada marca en el mismo commit que resuelve lo que describe.
 *
 * Despues: tests de controller, entidad Product, paginacion.
 */
// OK [§4]: un unico recurso con prefijo versionado, identificador estable y @Tag para
//          agrupar en Swagger UI. Verificado: 11 de 11 status codes correctos.
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Create, read, replace and delete inventory categories")
public class CategoryController {
    private final CategoryService categoryService;

    // OK: inyeccion por constructor con campo final. IoC/DI aplicado.
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(
            summary = "Create category",
            description = "Registers a new category and returns the created resource with the id assigned by the database. The Location header points to its URL. The name must be unique.",
            responses = {
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
        // OK [§4]: construye la URL desde la peticion actual en vez de concatenar a mano.
        //          Verificado: Location: http://localhost:8099/api/v1/categories/57
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
    // MEJORA [§2.4]: sin paginacion; devuelve la tabla entera. Con Category (decenas de
    //        filas) es YAGNI; hazlo con Product, donde los miles de filas son lo normal.
    //        Ojo al orden: cambia el contrato de List<T> a Page<T>, asi que hacerlo despues
    //        de anotar OpenAPI y de escribir los tests obliga a rehacer los dos.
    public List<CategoryDto> getAll() {
        List<CategoryDto> body = this.categoryService.getAllCategories();

        return body;
    }

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
        // OK [§4]: el bloqueante de la auditoria 3 esta cerrado. El service ya aplica los
        //          cambios ANTES de mapear, asi que la respuesta lleva los valores nuevos.
        //          Comprobar con: POST -> PUT con otro valor -> comparar respuesta con el GET.
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

        // OK [§4]: 204 sin cuerpo, correcto para un borrado.
        return ResponseEntity.noContent().build();
    }
}
