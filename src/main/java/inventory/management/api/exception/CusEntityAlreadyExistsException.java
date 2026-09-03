package inventory.management.api.exception;

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
