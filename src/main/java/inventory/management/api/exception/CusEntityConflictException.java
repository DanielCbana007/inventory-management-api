package inventory.management.api.exception;

public class CusEntityConflictException extends RuntimeException {
    public CusEntityConflictException(String message) {
        super(message);
    }

    public static CusEntityConflictException immutableField(String entityName, String field,
                                                            Object stored, Object received) {
        return new CusEntityConflictException(
                "%s %s cannot be changed: stored '%s', received '%s'"
                        .formatted(entityName, field, stored, received)
        );
    }
}
