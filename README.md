# Inventory Management API

REST API to manage an inventory of categories and products, built with Spring Boot.

## Stack

Java 21 · Spring Boot 4 · Spring Data JPA · PostgreSQL · Bean Validation · springdoc-openapi · JUnit 5 + Mockito

## Run it locally

You need Java 21 and a PostgreSQL database.

1. Copy `.env.example` to `.env` and fill in the database connection (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
2. Start the API:

   ```bash
   ./gradlew bootRun
   ```

It starts on `http://localhost:8080` with the `dev` profile, which creates or updates the schema
and loads sample data from `data.sql`. For the production settings:

```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/categories` | List categories (paginated) |
| `GET` | `/api/v1/categories/{id}` | Get a category |
| `POST` | `/api/v1/categories` | Create a category |
| `PUT` | `/api/v1/categories/{id}` | Replace a category |
| `DELETE` | `/api/v1/categories/{id}` | Delete a category without products |
| `GET` | `/api/v1/products` | List products (paginated) |
| `GET` | `/api/v1/products/{id}` | Get a product |
| `POST` | `/api/v1/products` | Create a product |
| `PUT` | `/api/v1/products/{id}` | Replace a product (the sku cannot change) |
| `DELETE` | `/api/v1/products/{id}` | Delete a product |

Listings accept `page`, `size` (max 100) and `sort`, for example `?page=0&size=20&sort=name,asc`.
Errors follow [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) (`application/problem+json`).

The full contract is in Swagger UI at `http://localhost:8080/swagger-ui.html` (`dev` profile only).

## Tests

```bash
./gradlew test
```

## Versions

See [CHANGELOG.md](CHANGELOG.md). Released under the [MIT License](LICENSE).
