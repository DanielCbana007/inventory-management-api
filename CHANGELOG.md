# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-09-22

First release: the inventory REST API, without authentication.

### Added

- Categories: create, list, get by id, replace and delete under `/api/v1/categories`.
- Products, each one in a required category: the same operations under `/api/v1/products`.
  The category is referenced by `categoryId` on input and returned embedded on output.
- `201 Created` responses carry a `Location` header that points to the new resource.
- Pagination on both listings with `page`, `size` and `sort`. Ordered by `id` by default,
  page size capped at 100, and only whitelisted fields can be used to sort.
- Errors returned as RFC 9457 Problem Details (`application/problem+json`). Validation errors
  list every rejected field with a stable `code` and a localized `message`.
- Business rules answered with `409 Conflict`: duplicate category name, duplicate product sku,
  an update that changes the sku, and deleting a category that still has products.
- Bean Validation on every input, aligned with the size of the database columns.
- OpenAPI 3 documentation and Swagger UI, enabled only in the `dev` profile.
- `dev` and `prod` profiles, with the database credentials read from `.env` or the environment.
- Unit tests for services, controllers and mappers (JUnit 5, Mockito, MockMvc).

[1.0.0]: https://github.com/DanielCbana007/inventory-management-api/releases/tag/v1.0.0
