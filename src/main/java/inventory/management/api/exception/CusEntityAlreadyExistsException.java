package inventory.management.api.exception;

public class CusEntityAlreadyExistsException extends RuntimeException {
    public CusEntityAlreadyExistsException(String message) {
        super(message);
    }

    // ERROR [§1.3]: la palabra "name" esta fija, pero Product choca por sku. Verificado:
    //        POST /api/v1/products con un sku repetido ->
    //        {"detail":"Product with name 'AUD-0005' already exists","status":409}
    //        AUD-0005 no es un nombre. Un cliente que ensene ese detail al usuario le senala
    //        un campo que no es. El campo en conflicto tiene que ser otro parametro.
    public static CusEntityAlreadyExistsException of(String entityName, Object value) {
        return new CusEntityAlreadyExistsException(
                "%s with name '%s' already exists".formatted(entityName, value)
        );
    }
}
