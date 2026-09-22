package inventory.management.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

// OK [§4]: extender ResponseEntityExceptionHandler en vez de capturar Exception es lo que
//     hace que 405 y 415 salgan ya en application/problem+json sin escribir un handler para
//     ellos. Verificado en la auditoria 6: los 29 casos de la matriz devuelven ProblemDetail.
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- excepciones de dominio ----------

    @ExceptionHandler(CusEntityNotFoundException.class)
    public ProblemDetail handleNotFound(CusEntityNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CusEntityAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExists(CusEntityAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CusEntityConflictException.class)
    public ProblemDetail handleConflict(CusEntityConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ---------- paginacion ----------

    @ExceptionHandler(CusInvalidSortException.class)
    public ProblemDetail handleInvalidSort(CusInvalidSortException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Red de seguridad por si la lista blanca de un controller nombra un campo que no existe en
    // la entidad: sin esto, la PropertyReferenceException llegaria al catch-all como 500.
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleUnknownSortProperty(PropertyReferenceException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Cannot sort by '%s': unknown property".formatted(ex.getPropertyName()));
    }

    // ---------- red de seguridad de la base de datos ----------

    // MEJORA [§3.2]: todo lo que rechaza la base sale como 409, tambien un texto demasiado largo
    //         o un numero que no cabe, que son 400. Si ves este 409 generico en el log, falta una
    //         validacion en el DTO: la regla es que la base no deberia tener que rechazar nada.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "Resource conflicts with an existing constraint");
    }

    // ---------- validación: sobrescribe al padre para detallar los campos ----------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationError(
                        fe.getField(),
                        fe.getCode(),
                        fe.getDefaultMessage()))
                .toList();

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(pd);
    }

    // ---------- último recurso ----------

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }
}
