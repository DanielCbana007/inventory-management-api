package inventory.management.api.exception;

public class CusEntityNotFoundException extends RuntimeException{
    public CusEntityNotFoundException(String message) {
        super(message);
    }

    public static CusEntityNotFoundException of(String entityName, Object id){
        return new CusEntityNotFoundException(
                "%s with id '%s' not found".formatted(entityName, id)
        );
    }
}
