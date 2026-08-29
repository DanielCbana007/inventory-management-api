package inventory.management.api.exception;

/**
 * Un fallo de validación sobre un campo concreto, tal como viaja en la propiedad
 * {@code errors} de la respuesta 400.
 *
 * La razón de que sean tres campos y no una frase: separa lo que lee la máquina de lo que lee
 * la persona.
 *
 *   field   -> qué input marcar en rojo.        Estable.
 *   code    -> qué regla falló (NotBlank, Size). Estable, siempre en inglés.
 *   message -> texto legible para logs y depuración.
 *              NO es estable: sale de los bundles de Hibernate Validator y cambia con la
 *              cabecera Accept-Language del cliente (es/en/de/ja...). Ningún cliente debe
 *              tomar decisiones comparando este texto; para eso está `code`.
 */
public record ValidationError(String field, String code, String message) {
}
