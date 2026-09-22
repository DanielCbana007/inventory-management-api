package inventory.management.api.product.controller;

import inventory.management.api.common.SortableFields;
import inventory.management.api.product.dto.ProductDto;
import inventory.management.api.product.dto.ProductRequestDto;
import inventory.management.api.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
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

/**
 * Leyenda de las marcas de auditoria: ver la cabecera de CategoryController.
 * Apuntan a docs/seguimiento/auditoria-5.md y el sufijo [§x] es su seccion.
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Create, read, replace and delete inventory products")
public class ProductController {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final SortableFields SORTABLE =
            SortableFields.of("id", "name", "sku", "price", "stock", "createdAt", "updatedAt");

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
                    @ApiResponse(responseCode = "201", description = "Product created",
                            headers = @Header(name = "Location", description = "URL of the created product",
                                    schema = @Schema(type = "string", format = "uri")),
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
            summary = "Get products, paginated",
            description = "Returns one page of products with the category of each one. page is zero-based, "
                    + "size defaults to 20 and is capped at 100, and the order is by id unless sort is given. "
                    + "Sortable fields: id, name, sku, price, stock, createdAt, updatedAt "
                    + "(e.g. sort=price,desc).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "One page of products"),
                    @ApiResponse(responseCode = "400", description = "sort names a field that is not sortable",
                            content = @Content(mediaType = PROBLEM_JSON,
                                    schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    public PagedModel<ProductDto> getAll(
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(this.service.getAllProducts(SORTABLE.check(pageable)));
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
