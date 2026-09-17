# Taro Allocation System

A distributed Spring Boot application for managing customers, taro types, and weekly taro allocations. It was developed for **COMP713 – Distributed Systems, Assessment 2 (Option A)**.

## Architecture

The application consists of two independent services with separate SQLite databases.

```text
Browser / API client
        |
        +--> Customer Service (port 8081) ----> customer.db
        |
        +--> Allocation Service (port 8082) --> allocation.db
                    |
                    +---- HTTP / JSON ----> Customer Service
```

| Service | Owns | Responsibilities |
| --- | --- | --- |
| Customer Service | `Customer`, `TaroType` | Customer and taro-type management, REST API, and web UI |
| Allocation Service | `WeeklyAllocation`, `AllocationItem` | Weekly allocation management, REST API, and web UI |

The Allocation Service stores customer and taro-type IDs only. It uses HTTP requests to Customer Service to validate those IDs and retrieve customer names, taro names, and standard prices; it never reads `customer.db` directly.

## Data and business rules

- Customer and taro-type names are normalized and unique.
- A supplied phone number is optional, but when present must be an optional `+` followed by 7–15 digits.
- An allocation contains one or more items; each taro type may occur only once.
- Quantity must be a positive whole number. Fractional, zero, and negative quantities are rejected.
- `pricePerKg` is optional. When omitted, the taro type's current standard price is used; a supplied price is retained on the allocation item for historical accuracy.
- Supplied allocation dates are normalized to the Monday of their week. A customer can therefore have only one allocation in a calendar week.
- Deleting an allocation also deletes its allocation items.

## APIs

Both services expose JSON REST APIs. The full contracts are available at [customer-service/contracts/customer-api.yaml](customer-service/contracts/customer-api.yaml) and [allocation-service/contracts/allocation-api.yaml](allocation-service/contracts/allocation-api.yaml).

### Customer Service — port 8081

| Resource | Operations |
| --- | --- |
| Customers | `GET /api/v1/customers`, `GET /api/v1/customers/{id}`, `POST /api/v1/customers`, `PUT /api/v1/customers/{id}` |
| Taro types | `GET /api/v1/taro-types`, `GET /api/v1/taro-types/{id}`, `POST /api/v1/taro-types`, `PUT /api/v1/taro-types/{id}` |

### Allocation Service — port 8082

| Resource | Operations |
| --- | --- |
| Weekly allocations | `GET /api/v1/allocations`, `GET /api/v1/allocations/{id}`, `POST /api/v1/allocations`, `PUT /api/v1/allocations/{id}`, `DELETE /api/v1/allocations/{id}` |

Successful deletion returns `204 No Content`. A missing allocation returns `404 WEEKLY_ALLOCATION_NOT_FOUND`.

The Allocation Service URL defaults to `http://localhost:8081` and can be configured without code changes:

```properties
customer.service.url=${CUSTOMER_SERVICE_URL:http://localhost:8081}
```

## Web interfaces

Both services use Thymeleaf for server-rendered pages. Important validation and business rules remain enforced by the backend services.

| Service | Server-rendered pages |
| --- | --- |
| Customer Service | `/customers`, `/customers/new`, `/customers/{id}/edit`, `/taro-types`, `/taro-types/new`, `/taro-types/{id}/edit` |
| Allocation Service | `/allocations`, `/allocations/new`, `/allocations/{id}/edit` |

Customer Service also provides client-rendered customer and taro-type pages. Allocation Service now provides the equivalent client-rendered allocation list, create, and edit pages at `/allocations-client.html`, `/allocation-new-client.html`, and `/allocation-edit-client.html`. These pages use `fetch()` against their JSON APIs, with JavaScript kept in each service's `src/main/resources/static/js` directory.

Allocation create and edit pages support dynamic add/remove rows for multiple taro types, an optional custom price, existing customer and taro names, and deletion. The Allocation Service interfaces display customer and taro names rather than exposing their IDs to users.

## Validation and errors

Both services return structured JSON errors:

```json
{
  "code": "INVALID_QUANTITY",
  "message": "Quantity must be a positive whole number",
  "path": "/api/v1/allocations"
}
```

Allocation errors include validation failures (`400`), missing customers, taro types, or allocations (`404`), duplicate weekly allocations (`409`), unavailable Customer Service (`503`), and unexpected server errors (`500`). Duplicate messages use the relevant customer or taro name where available, for example: `Weekly allocation already exists for Island Foods for week starting 2026-09-14`.

## Running the application

Java 21 is required. Start Customer Service first, then Allocation Service in a separate terminal.

```bash
cd customer-service
./mvnw spring-boot:run
```

```bash
cd allocation-service
./mvnw spring-boot:run
```

The services run at `http://localhost:8081` and `http://localhost:8082` respectively. Each service persists normal application data in its own SQLite database. Test databases are kept under the corresponding `target` directory.

## Testing

Run the suites independently:

```bash
cd customer-service
./mvnw test
```

```bash
cd allocation-service
./mvnw test
```

Latest full test run:

```text
Customer Service tests:   27
Allocation Service tests: 38
Total automated tests:    65
Failures: 0, Errors: 0, Skipped: 0
```

Customer Service tests cover customer/taro CRUD, validation, duplicate handling, persistence, and concurrent duplicate creation. Allocation Service tests cover creation, retrieval, updates, custom/default pricing, multiple items, week normalization, duplicate protection, friendly duplicate messages, whole-number quantity validation, deletion and child-item cleanup, downstream failures, persistence, and concurrent allocation creation.

Manual integration commands for the two running services are documented in [allocation_test.md](allocation_test.md).

## Technology stack

- Java 21, Spring Boot 4.1.1, Spring Web, Spring REST Client, Spring Data JPA, Jakarta Validation, Hibernate
- Thymeleaf, HTML, and JavaScript
- SQLite and Maven
- OpenAPI / Swagger
- JUnit 5, Mockito, and MockMvc

## Development status

Completed: both REST APIs; Customer Service server- and client-rendered interfaces; Allocation Service server- and client-rendered list/create/edit/delete interfaces; service-to-service HTTP communication; validation, structured errors, OpenAPI contracts, frontend end-to-end testing, and automated/manual testing.

Remaining major work: consistent CSS.
