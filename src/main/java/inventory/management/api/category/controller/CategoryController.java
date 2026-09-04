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
 * ---- UNICO EJE POR DEBAJO DE HABILITA: OpenAPI ----
 *  1. [§1.3] Los @ApiResponse declaran status que la API no devuelve.  <-- aqui
 *  3. [§1.5] ProblemDetail no aparece en los esquemas publicados.
 *
 * Despues: tests de controller, entidad Product, paginacion.
 */
// OK [§4]: un unico recurso con prefijo versionado, identificador estable y @Tag para
//          agrupar en Swagger UI. Verificado: 11 de 11 status codes correctos.
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "All methods to categories")
public class CategoryController {
    private final CategoryService categoryService;

    // OK: inyeccion por constructor con campo final. IoC/DI aplicado.
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(
            summary = "Create category",
            description = "create a new category",
            responses = {
                    // ERROR [§1.3]: este endpoint NUNCA devuelve 200. Devuelve 201 (verificado
                    //        con curl: HTTP 201 + Location). Un cliente que trate cualquier codigo
                    //        distinto de 200 como error rechazara todas las creaciones correctas.
                    //        Ademas: "cteated" -> "created", y ese texto se ve en Swagger UI.
                    @ApiResponse(responseCode = "200", description = "Category cteated"),
                    // ERROR [§1.3]: "Category exists" describe un 409, no un 400. El 400 es para
                    //        datos invalidos (name vacio, en blanco o de mas de 100 caracteres).
                    // FALTA [§1.3]: no se declara el 409, que ahora SI ocurre desde el service
                    //        (existsByName + CusEntityAlreadyExistsException).
                    // FALTA [§1.5]: content = @Content vacio, asi que el contrato no dice que los
                    //        errores llegan como ProblemDetail (RFC 9457) con un array `errors`.
                    //        Se declara con schema = @Schema(implementation = ProblemDetail.class)
                    //        y mediaType = "application/problem+json".
                    @ApiResponse(responseCode = "400", description = "Category exists", content = @Content)
            }
    )
    public ResponseEntity<CategoryDto> create(
            // MEJORA [§3]: descripcion vacia y nombre completamente cualificado en vez de un
            //        import. Si no aporta nada, quitala.
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "")
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
            description = "It includes all the app's categories.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "list of categories"),
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
            description = "ID of the category to be updated",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated category"),
                    // FALTA [§1.3]: no se declara el 400, que si ocurre (name invalido, o un id
                    //        no numerico en la ruta). Verificado con curl.
                    // FALTA [§1.5]: content vacio; falta declarar ProblemDetail.
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
        // OK [§4]: el bloqueante de la auditoria 3 esta cerrado. El service ya aplica los
        //          cambios ANTES de mapear, asi que la respuesta lleva los valores nuevos.
        //          Comprobar con: POST -> PUT con otro valor -> comparar respuesta con el GET.
        CategoryDto body = this.categoryService.updateCategory(requestDto, id);

        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete category",
            description = "ID of the category to be delete",
            responses = {
                    // ERROR [§1.3]: este endpoint NUNCA devuelve 200. Devuelve 204 No Content
                    //        (verificado). Y faltan el 404 y el 400, que si ocurren.
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

        // OK [§4]: 204 sin cuerpo, correcto para un borrado.
        return ResponseEntity.noContent().build();
    }

    // FALTA [§2.3]: sin CategoryControllerTest. Los 11 casos ya verificados con curl son
    //        el guion: 201 crear - 200 listar - 404 put y delete inexistentes - 400 sin name -
    //        400 name en blanco - 400 name largo - 409 duplicado - 400 json roto -
    //        400 id no numerico - 405 verbo no soportado.
    //        Se escriben con @WebMvcTest(CategoryController.class) + MockMvc + @MockitoBean
    //        del service. Ojo: @MockBean ya no existe en Spring 7.
}
