package inventory.management.api.product.controller;

import inventory.management.api.product.dto.ProductDto;
import inventory.management.api.product.dto.ProductRequestDto;
import inventory.management.api.product.service.ProductService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Leyenda de las marcas de revision: ver la cabecera de CategoryController.
 * Apuntan a las notas de la revision 5 y el sufijo [§x] es su seccion.
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Create, read, replace and delete inventory products")
public class ProductController {

    private static final String PROBLEM_JSON = "application/problem+json";

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Create product",
            description = "Registers a new product and returns the created resource with the id "
                    + "assigned by the database. The Location header points to its URL. "
                    + "The sku must be unique and categoryId must reference an existing category.",
            responses = {
                    // MEJORA [§3.4]: el 201 devuelve una cabecera Location y aqui no se declara.
                    //         Un generador de clientes lee el contrato, no la prosa.
                    @ApiResponse(responseCode = "201", description = "Product created",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProductDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid payload: a required field is missing, the name or sku length is out of range, or price or stock is negative",
                            content = @Content(mediaType = PROBLEM_JSON,
                                    schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "404", description = "No category exists with the given categoryId",
                            content = @Content(mediaType = PROBLEM_JSON,
                                    schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "409", description = "A product with that sku already exists",
                            content = @Content(mediaType = PROBLEM_JSON,
                                    schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public ResponseEntity<ProductDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product to create. The id, createdAt and updatedAt fields are assigned by the server.")
            @RequestBody @Valid ProductRequestDto requestDto) {
        ProductDto created = this.service.createProduct(requestDto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(
            summary = "Get all products",
            description = "Returns the whole catalogue with the category of each product. Not paginated yet.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of products",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = ProductDto.class))))
            }
    )
    // MEJORA [§2.3]: sin paginacion. Aqui pesa mas que en Category: un catalogo crece sin
    //         techo. Va DESPUES de los tests, porque cambia el contrato a Page<T>.
    public List<ProductDto> getAll() {
        return this.service.getAllProducts();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = "Returns a single product with its category. This is the URL the Location "
                    + "header of a POST points to.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProductDto.class))),
                    @ApiResponse(responseCode = "400", description = "The id is not a valid number",
                            content = @Content(mediaType = PROBLEM_JSON,
                                    schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "404", description = "No product exists with that id",
                            content = @Content(mediaType = PROBLEM_JSON,
                                    schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public ProductDto getById(
            @Parameter(name = "id", description = "Id of the product to get",
                    example = "3", required = true)
            @PathVariable Long id) {
        return this.service.getProductById(id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update product by ID",
            description = "Replaces the product as a whole. This is a PUT, not a PATCH: fields you do not "
                    + "send are set to null, they do not keep their previous value. The sku cannot be "
                    + "changed: send the current one, or the request is rejected with 409.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProductDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid payload, or the id is not a number",
                            content = @Content(mediaType = PROBLEM_JSON,
                                    schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "404", description = "No product exists with that id, or no category exists with the given categoryId",
                            content = @Content(mediaType = PROBLEM_JSON,
                                    schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "409", description = "The sku sent differs from the stored one: it cannot be changed",
                            content = @Content(mediaType = PROBLEM_JSON,
                                    schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public ResponseEntity<ProductDto> update(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New state of the product. The sku must match the stored one.")
            @RequestBody @Valid ProductRequestDto requestDto,
            @Parameter(name = "id", description = "Id of the product to replace",
                    example = "3", required = true)
            @PathVariable Long id) {
        ProductDto body = this.service.updateProduct(requestDto, id);

        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete product",
            description = "Deletes the given product. Returns no body.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Product deleted",
                            content = @Content),
                    @ApiResponse(responseCode = "400", description = "The id is not a valid number",
                            content = @Content(mediaType = PROBLEM_JSON,
                                    schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "404", description = "No product exists with that id",
                            content = @Content(mediaType = PROBLEM_JSON,
                                    schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public ResponseEntity<Void> delete(
            @Parameter(name = "id", description = "Id of the product to delete",
                    example = "3", required = true)
            @PathVariable Long id) {
        this.service.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
}
