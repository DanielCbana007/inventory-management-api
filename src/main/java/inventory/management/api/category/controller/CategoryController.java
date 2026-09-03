package inventory.management.api.category.controller;

import inventory.management.api.category.dto.CategoryDto;
import inventory.management.api.category.dto.CategoryRequestDto;
import inventory.management.api.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/*
 * ==== PENDIENTE - docs/seguimiento/auditoria-2.md ====
 *
 * TODO   : hay que hacerlo. [§x] apunta a la sección de la auditoría.
 * MEJORA : opcional. No bloquea, pero es lo que separa Habilita de Domina.
 *
 * Borra cada marca en el mismo commit que la resuelve.
 *
 * ---- ORDEN QUE MAS DESBLOQUEA ----
 *  4. [§2.2] Entidad Product y su relación con Category.
 *  5. [§2.4] Paginación en el listado.
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "All methods to categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(
            summary = "Create category",
            description = "create a new category",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category cteated"),
                    @ApiResponse(responseCode = "400", description = "Category exists", content = @Content)
            }
    )
    public ResponseEntity<CategoryDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "")
            @RequestBody @Valid CategoryRequestDto requestDto) {
        CategoryDto created = this.categoryService.createCategory(requestDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(
            summary = "Get all categories",
            description = "It includes all the app's categories.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "list of categories"),
            }
    )
    // TODO [§2.4]: cambia la firma a Page<CategoryDto> getAll(Pageable pageable) y pasa el
    //              Pageable al service. Hoy devuelves la tabla entera.
    public List<CategoryDto> getAll() {
        List<CategoryDto> body = this.categoryService.getAllCategories();

        return body;
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update category by ID",
            description = "ID of the category to be updated",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated category"),
                    @ApiResponse(responseCode = "404", description = "Not found category", content = @Content)
            }
    )
    public ResponseEntity<CategoryDto> update(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Specify which values you want to update.")
            @RequestBody @Valid CategoryRequestDto requestDto,
            @Parameter(
                    name = "id",
                    description = "ID of category to update",
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
            description = "ID of the category to be delete",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category removed"),
            }
    )
    public ResponseEntity<Void> delete(
            @Parameter(
                    name = "id",
                    description = "category ID",
                    example = "3",
                    required = true
            )
            @PathVariable Long id
    ) {
        this.categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }

    // TODO [§2.3]: anota esta clase con @Tag y cada método con @Operation y @ApiResponse.
    //              Sin eso springdoc publica la entidad como esquema: documentas tu tabla,
    //              no tu contrato. Es competencia JUNIOR y está en Deficiente.

    // TODO [§2.1]: crea CategoryControllerTest con @WebMvcTest + MockMvc y un mock del
    //              service. Los 10 casos que ya verificaste con curl son el guion:
    //              201 crear - 200 listar - 404 put y delete inexistentes - 400 sin name -
    //              400 name en blanco - 400 name largo - 409 duplicado - 400 json roto -
    //              400 id no numérico - 405 verbo no soportado.
}
