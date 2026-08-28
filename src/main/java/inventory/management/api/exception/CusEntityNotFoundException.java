package inventory.management.api.exception;

public class EntityNotFoundException extends RuntimeException{
    public EntityNotFoundException(String message) {
        super(message);
    }

    public static EntityNotFoundException of(String entityName, Object id){
        return new EntityNotFoundException(
                "%s with id '%s' not found".formatted(entityName, id)
        );
    }
}
