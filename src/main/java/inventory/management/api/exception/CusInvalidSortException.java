package inventory.management.api.exception;

import java.util.Collection;
import java.util.stream.Collectors;

public class CusInvalidSortException extends RuntimeException {
    public CusInvalidSortException(String message) {
        super(message);
    }

    public static CusInvalidSortException of(String property, Collection<String> sortable) {
        return new CusInvalidSortException(
                "Cannot sort by '%s'. Sortable fields: %s".formatted(
                        property, sortable.stream().sorted().collect(Collectors.joining(", ")))
        );
    }
}
