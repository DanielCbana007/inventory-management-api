package inventory.management.api.common;

import inventory.management.api.exception.CusInvalidSortException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

// Lista blanca de los campos por los que el cliente puede ordenar. Sin ella, ?sort= acepta
// cualquier atributo de la entidad, incluidas las colecciones: ordenar por una obliga a un JOIN
// que duplica filas, y el OFFSET de la paginacion corta sobre esas filas duplicadas.
public final class SortableFields {
    private final Set<String> fields;

    private SortableFields(Set<String> fields) {
        this.fields = fields;
    }

    public static SortableFields of(String... fields) {
        return new SortableFields(Set.of(fields));
    }

    public Pageable check(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!this.fields.contains(order.getProperty())) {
                throw CusInvalidSortException.of(order.getProperty(), this.fields);
            }
        }
        return pageable;
    }
}
