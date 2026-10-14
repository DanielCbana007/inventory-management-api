# Proyecto 2 — Cómo trabajamos y qué decidimos

Autenticación y autorización sobre el inventario del Proyecto 1. Se publicará como `2.0.0`
porque JWT rompe el contrato de todos los endpoints.

> **Los tickets viven en GitHub Issues, no aquí.** Este documento guarda lo que no cabe en una
> issue: convenciones, DoR/DoD y el registro de decisiones. Si algo tiene estado, va a Issues.

## Convenciones

- **Una secuencia de claves por proyecto**, indiferente al tipo: `RMP-0003` puede ser tarea
  técnica y `RMP-0005` historia de usuario. La clave numera tickets, no ramas ni historias.
  Si no es única se rompe lo único para lo que existe: trazar rama → PR → ticket.
- **Rama por incremento:** `feature/RMP-000X-slug-corto`. Clave para trazar, slug para leer.
  `RMP-0001` y `RMP-0002` se quedan sin slug: no se reescribe historia pasada para que case
  con una convención nueva.
- **Cardinalidad:** 1 rama → 1 PR → 1 ticket. Una HU puede necesitar varias ramas, y una rama
  puede no colgar de ninguna HU (los *enablers*).
- **El orden es de ejecución, no de tipo.** Agrupar las cinco tareas técnicas primero daría
  numeración bonita y cuatro semanas sin entregar nada que un usuario note.

## Labels

| Label | Color | Significado |
|---|---|---|
| `user-story` | `#0E8A16` | Aporta valor a un usuario final |
| `tech-task` | `#1D76DB` | Permite la entrega, sin valor directo para el usuario |
| `project-2` | `#FBCA04` | Autenticación y autorización |
| `security` | `#B60205` | Spring Security, JWT, roles |
| `testing` | `#5319E7` | Tests e infraestructura de pruebas |
| `infra` | `#C2E0C6` | Docker, CI, despliegue |
| `docs` | `#BFD4F2` | OpenAPI y documentación |
| `stretch` | `#D4C5F9` | Fuera del alcance mínimo; solo si sobra tiempo |

**Milestone:** `Proyecto 2 · v2.0.0`

## Definition of Ready y Definition of Done

Son **únicas y aplican a todos los tickets**. Lo que cambia por ticket son los criterios de
aceptación. Escribir una DoD distinta por tarjeta es confundirla con criterios de aceptación.

### DoR — un ticket no entra a rama hasta que

- Tiene criterios de aceptación verificables con `curl` (status code + cuerpo), no "debe funcionar bien".
- Sabes nombrar la decisión de diseño que contiene y su alternativa descartada.
- No depende de otro ticket sin mergear.

### DoD — un PR no se mergea a `develop` hasta que

- Suite en verde, incluidos los 70 tests que ya existían.
- Los casos de error verificados **en ejecución real**, no razonados.
- **El historial muestra el commit con el test en rojo antes del que lo pone en verde.**
  Es lo que convierte "hice TDD" en demostrable: se ve en `git log`.
- Tests con patrón AAA.
- OpenAPI regenerado y comprobado contra `/v3/api-docs`.
- Commits atómicos en inglés, Conventional Commits, historial limpio.
- Ningún secreto nuevo fuera de `.env`, con su entrada en `.env.example`.
- Puedes explicar el porqué del incremento en voz alta sin mirar el código.

## Los once tickets

El detalle está en las issues. Aquí solo el orden y la semana.

| # | Tipo | Qué | Semana |
|---|---|---|---|
| RMP-0003 | tech | Modelo de datos `User`, `Role` y refresh tokens | S5 |
| RMP-0004 | tech | Testcontainers | S5/S6 |
| RMP-0005 | HU | Registro de cuenta | S6 |
| RMP-0006 | HU | Autenticación con JWT | S6 |
| RMP-0007 | tech | Adaptar la suite a la cadena de seguridad | S6 |
| RMP-0008 | HU | Autorización por rol | S6 |
| RMP-0009 | HU | Renovación del token | S6/S7 |
| RMP-0010 | HU | Contrato de seguridad en OpenAPI | S7 |
| RMP-0011 | tech | Docker y Compose | S7 |
| RMP-0012 | tech | Pipeline de CI | S7 |
| RMP-0013 | HU | Login con Google (stretch) | S8 |

---

# Registro de decisiones

Diecisiete decisiones tomadas el **22 de septiembre de 2026**, antes de abrir las issues.
Para cada una: qué se eligió y **qué se pierde**. Lo segundo es lo que se defiende en entrevista;
saber solo lo primero es nivel *Inicial*.

## Identidad y modelo de datos

**A1 · El refresh token se persiste en tabla.**
No por CSRF —que es un eje independiente— sino porque C3 exige rotación, y no se puede
invalidar lo que no se guarda. *Se pierde:* deja de ser stateless y cada refresh toca la base.

**A2 · Access 15 min, refresh 7 días.**
El access es corto porque **no se puede revocar**: 15 minutos es la ventana máxima que se acepta
que un token robado siga sirviendo. *Se pierde:* más llamadas a `/auth/refresh`.

**A3 · El `sub` lleva el id del usuario.**
*Se pierde:* el id es enumerable y revela el volumen de usuarios. *Se evita:* que un cambio de
email deje todos los tokens vivos apuntando a un identificador obsoleto.

**A4 · Rol por defecto STAFF, asignado en el servidor.**
Principio de menor privilegio. El DTO de registro **no tiene campo `role`**: aceptarlo sería
escalada de privilegios, *Insuficiente* en la escala. El primer ADMIN nace como dato semilla del
perfil `dev`; en producción se sembraría por variable de entorno al arrancar.
*Limitación consciente:* no existe endpoint para cambiar el rol de nadie.

**A5 · Se mantiene `ddl-auto=update` por ahora.**
*Se pierde:* los tests de `RMP-0004` corren contra el esquema que Hibernate infiere de las
entidades, no contra el de Neon. Si divergen, el test miente. Se asume y se declara.

## Emisión y transporte del token

**B1 · jjwt, con filtro propio.**
Spring Security trae un *resource server* OAuth2 que valida JWT sin escribir filtro, pero el
brief exige filtro propio. Existe la vía corta y se descarta a conciencia.

**B2 · HS256.**
Simétrico: la misma clave firma y verifica, luego quien pueda validar puede emitir. Irrelevante
mientras una sola aplicación hace ambas cosas. *Se pierde:* el día que un segundo servicio
valide tokens habría que darle la clave de firma — ahí tocaría RS256, que separa privada y
pública. *Implementación:* HS256 exige clave de **256 bits (32 bytes) mínimo**; jjwt lanza
`WeakKeyException` con menos. Bytes aleatorios en base64, en `.env`.

**B3 · El rol viaja dentro del token.**
Evita una consulta por request. *Se pierde:* degradar a alguien de ADMIN a STAFF no surte efecto
hasta que su token expire — con A2, hasta 15 minutos. A2 y B3 son la misma conversación.

**B4 · Token en el cuerpo de la respuesta y en la cabecera `Authorization`.**
El navegador no adjunta esa cabecera automáticamente, así que **no hay CSRF** y desactivarlo
tiene argumento. *Se pierde:* el token acaba en `localStorage`, expuesto a **XSS**. El trade-off
real no es seguro contra inseguro, es **CSRF contra XSS**.

**B5 · Contraseña de 12 caracteres mínimo, sin reglas de complejidad. BCrypt strength 12.**
La longitud vence a la complejidad. El *strength* es un parámetro de coste: cada punto duplica
el tiempo de hash. *Se pierde:* latencia en el login. Es seguridad contra tiempo de respuesta,
no un número mágico.

**B6 · El registro no devuelve token; obliga a hacer login.**
Un solo camino de emisión. *Se pierde:* una llamada extra de cara al usuario.

## Autorización

**C1 · Las reglas de patrón claro van en el `SecurityFilterChain`.**
Escrituras sobre `/products` y `/categories` declaradas en un solo sitio y visibles de un vistazo.
*Se pierde:* cuando la regla depende del objeto y no de la ruta, la cadena no alcanza y hace falta
`@PreAuthorize`. Lo que no vale es mezclar sin criterio.

**C2 · `hasAuthority`, sin prefijo `ROLE_`.**
Regla de coherencia obligatoria: en base se guarda `ADMIN`; las autoridades se construyen
literales con `new SimpleGrantedAuthority(role.name())`; se comprueba siempre con
`hasAuthority('ADMIN')` y **nunca** con `hasRole` en ningún punto del proyecto.
*Trampa:* atajos como `User.withUsername(...).roles("ADMIN")` añaden `ROLE_` por su cuenta y
producen **403 en todo** sin mensaje que lo explique.

**C3 · El refresh rota.**
Cada uso emite uno nuevo e invalida el anterior; un refresh ya usado que reaparece es señal de
robo. *Se pierde:* si el cliente pierde la respuesta por un fallo de red, se queda fuera.

**C4 · 401 y 403 se unifican con `AuthenticationEntryPoint` y `AccessDeniedHandler`.**
Son dos fallos distintos en momentos distintos: el *entry point* actúa cuando **no sé quién eres**
(401), el *access denied handler* cuando **sé quién eres y no puedes** (403). Son autenticación y
autorización materializadas en dos objetos.
*Por qué existen:* `@RestControllerAdvice` es de Spring MVC y vive en el `DispatcherServlet`;
estos errores ocurren antes, en la cadena de filtros de servlet. Esa frontera es donde acaba
Spring Security y empieza Spring MVC.

## Infraestructura

**D1 · El Compose levanta un Postgres local.**
*Se pierde:* no prueba contra Neon. *Se gana:* `docker compose up` es autónomo y no necesita
credenciales de una base externa.

**D2 · Un email registrado con contraseña que entre por Google se vincula a la cuenta existente.**
*Se pierde:* si alguien pudiera registrar un email ajeno antes que su dueño, la vinculación le
daría acceso. Exige que el proveedor confirme el email como verificado.
Aplica solo si se llega a `RMP-0013`.
