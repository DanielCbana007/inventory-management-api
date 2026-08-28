package inventory.management.api.exception;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String message) {
        super(message);
    }

    public static EntityAlreadyExistsException of(String entityName, Object id) {
        return new EntityAlreadyExistsException(
                "%s with id '%s' not found".formatted(entityName, id)
        );
    }
}
