package inventory.management.api.category.dto;

// OK [§4]: DTO de SALIDA. Si lleva id: es el servidor diciendo al cliente como referirse
//          al recurso. Mismo campo que en el de entrada, direccion opuesta, regla opuesta.
// MEJORA [§3]: sin @Schema. El id podria marcarse accessMode = READ_ONLY para que Swagger
//        lo muestre en las respuestas y lo omita de los formularios.
public record CategoryDto(
        Long id,
        String name,
        String description
) {
}
