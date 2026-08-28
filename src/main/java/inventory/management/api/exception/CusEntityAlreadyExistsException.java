package inventory.management.api.exception;

// TODO [§1.1]: esta excepción tiene handler pero nunca se lanza. La activarás al añadir
//              la comprobación de duplicado en CategoryService.createCategory.
public class CusEntityAlreadyExistsException extends RuntimeException {
    public CusEntityAlreadyExistsException(String message) {
        super(message);
    }

    public static CusEntityAlreadyExistsException of(String entityName, Object value) {
        return new CusEntityAlreadyExistsException(
                "%s with name '%s' already exists".formatted(entityName, value)
        );
    }
}
