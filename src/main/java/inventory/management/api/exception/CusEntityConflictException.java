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

    public static CusEntityConflictException hasDependents(String entityName, Object id, String dependents) {
        return new CusEntityConflictException(
                "%s with id '%s' still has %s".formatted(entityName, id, dependents)
        );
    }
}
