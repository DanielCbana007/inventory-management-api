package inventory.management.api.exception;

public class CusEntityAlreadyExistsException extends RuntimeException {
    public CusEntityAlreadyExistsException(String message) {
        super(message);
    }

    public static CusEntityAlreadyExistsException of(String entityName, String field, Object value) {
        return new CusEntityAlreadyExistsException(
                "%s with %s '%s' already exists".formatted(entityName, field, value)
        );
    }
}
