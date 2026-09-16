# Taro Allocation System

A distributed web/API application for managing customers, taro types, and weekly taro allocations.

This project is being developed for **COMP713 – Distributed Systems, Assessment 2 (Option A)**.

## Overview

The Taro Allocation System manages weekly taro allocations for customers.

The application is split into two independent Spring Boot services:

* **Customer Service** – manages customers and taro types.
* **Allocation Service** – manages weekly allocations and allocation items.

The Allocation Service communicates with the Customer Service using HTTP REST requests. Each service owns its own SQLite database and does not directly access the other service's data.

## Architecture

```text
Client
  |
  | HTTP / JSON
  v
Allocation Service
Port 8082
  |
  | HTTP / JSON
  v
Customer Service
Port 8081
```

Each service manages its own SQLite database:

```text
customer-service
├── Customer
└── TaroType

allocation-service
├── WeeklyAllocation
└── AllocationItem
```

The Allocation Service stores only the IDs of customers and taro types owned by the Customer Service.

When an allocation is requested, the Allocation Service communicates with the Customer Service to validate those IDs and retrieve information such as customer names, taro type names, and standard prices.

For example:

```text
GET /api/v1/allocations/1

        |
        v

Allocation Service
        |
        ├── reads allocation.db
        |
        └── HTTP requests to Customer Service
              ├── resolve customer
              └── resolve taro types
```

This keeps ownership of data separated between the two services.

## Data Model

### Customer

Stores information about businesses receiving taro.

Fields include:

* `id`
* `name`
* `contactName`
* `phone`
* `active`

Customer names are normalized and protected by a database uniqueness constraint to prevent duplicate customers, including during concurrent requests.

### TaroType

Stores the different types of taro available.

Fields include:

* `id`
* `name`
* `description`
* `standardPrice`

The standard price acts as the default price when creating or updating an allocation item if no custom price is supplied.

Taro type names are also protected by a database uniqueness constraint.

### WeeklyAllocation

Represents a customer's allocation for a particular week.

Fields include:

* `id`
* `customerId`
* `weekStart`

A customer can only have one allocation for a particular week.

The database protects the combination:

```text
customerId + weekStart
```

to prevent duplicate weekly allocations.

### AllocationItem

Represents a particular taro type within a weekly allocation.

Fields include:

* `id`
* `weeklyAllocationId`
* `taroTypeId`
* `quantity`
* `pricePerKg`

The price is stored directly on each allocation item so that:

* different customers can receive different prices;
* prices can change between weeks;
* historical allocation prices remain unchanged if a taro type's standard price later changes.

If `pricePerKg` is omitted when creating or updating an allocation, the current `standardPrice` from the Customer Service is used automatically.

The same taro type cannot appear more than once inside a single weekly allocation.

## Technologies

* Java 21
* Spring Boot 4.1.1
* Spring Web
* Spring REST Client
* Spring Data JPA
* Jakarta Validation
* Hibernate
* SQLite
* Maven
* OpenAPI / Swagger
* JUnit 5
* Mockito
* MockMvc

## Project Structure

```text
taro-allocation-system/
├── customer-service/
│   ├── contracts/
│   │   └── customer-api.yaml
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── allocation-service/
│   ├── contracts/
│   │   └── allocation-api.yaml
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── frontend/
│
├── allocation_test.md
└── README.md
```

## Customer Service API

The Customer Service runs on port `8081`.

### Customer Endpoints

```text
GET  /api/v1/customers
GET  /api/v1/customers/{id}
POST /api/v1/customers
PUT  /api/v1/customers/{id}
```

Supported operations include:

* retrieving all customers;
* retrieving a customer by ID;
* creating customers;
* updating customers;
* rejecting invalid customer data;
* preventing duplicate customer names;
* protecting duplicate creation during concurrent requests.

### Taro Type Endpoints

```text
GET  /api/v1/taro-types
GET  /api/v1/taro-types/{id}
POST /api/v1/taro-types
PUT  /api/v1/taro-types/{id}
```

Supported operations include:

* retrieving all taro types;
* retrieving a taro type by ID;
* creating taro types;
* updating taro types;
* validating standard prices;
* preventing duplicate taro type names;
* protecting duplicate creation during concurrent requests.

The full API contract is available in:

```text
customer-service/contracts/customer-api.yaml
```

## Allocation Service API

The Allocation Service runs on port `8082`.

### Allocation Endpoints

```text
GET  /api/v1/allocations
GET  /api/v1/allocations/{id}
POST /api/v1/allocations
PUT  /api/v1/allocations/{id}
```

Supported operations include:

* retrieving all weekly allocations;
* retrieving a weekly allocation by ID;
* creating weekly allocations;
* updating existing weekly allocations;
* replacing allocation items during an update;
* assigning multiple taro types to an allocation;
* using customer-specific weekly prices;
* automatically using the standard taro price when no custom price is supplied;
* preventing duplicate taro types inside an allocation;
* preventing duplicate allocations for the same customer and week;
* validating customer and taro type IDs through the Customer Service;
* preserving existing allocation data when an update fails.

The full API contract is available in:

```text
allocation-service/contracts/allocation-api.yaml
```

## Distributed Service Communication

The Allocation Service does not directly access the Customer Service database.

Instead, it uses HTTP requests such as:

```text
GET /api/v1/customers/{id}
GET /api/v1/taro-types/{id}
```

These requests are used to:

* verify that customers exist;
* verify that taro types exist;
* retrieve customer names;
* retrieve taro type names;
* retrieve standard taro prices.

The Customer Service URL is externally configurable:

```properties
customer.service.url=${CUSTOMER_SERVICE_URL:http://localhost:8081}
```

By default, the Allocation Service connects to:

```text
http://localhost:8081
```

The service URL can be changed using the `CUSTOMER_SERVICE_URL` environment variable without modifying the application source code.

## Error Handling

Both services return structured JSON errors.

Example:

```json
{
  "code": "CUSTOMER_NOT_FOUND",
  "message": "Customer not found with id: 999",
  "path": "/api/v1/allocations"
}
```

The Allocation Service handles errors including:

| Status | Error                                    |
| ------ | ---------------------------------------- |
| `400`  | Validation error                         |
| `400`  | Duplicate taro type within an allocation |
| `404`  | Customer not found                       |
| `404`  | Taro type not found                      |
| `404`  | Weekly allocation not found              |
| `409`  | Weekly allocation already exists         |
| `503`  | Customer Service unavailable             |
| `500`  | Unexpected server error                  |

### Downstream Service Failure

If the Customer Service becomes unavailable, the Allocation Service remains running and returns:

```text
HTTP 503 Service Unavailable
```

with a structured response such as:

```json
{
  "code": "CUSTOMER_SERVICE_UNAVAILABLE",
  "message": "Customer service is currently unavailable",
  "path": "/api/v1/allocations/1"
}
```

This prevents a downstream connection failure from being exposed as an unhandled server error.

## Running the Application

Both services require Java 21.

### 1. Start Customer Service

Open a terminal:

```bash
cd customer-service
./mvnw spring-boot:run
```

The Customer Service will run on:

```text
http://localhost:8081
```

### 2. Start Allocation Service

Open another terminal:

```bash
cd allocation-service
./mvnw spring-boot:run
```

The Allocation Service will run on:

```text
http://localhost:8082
```

Both services can now communicate over HTTP.

## Example Allocation

Assuming customer `1` and taro type `1` exist:

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "weekStart": "2026-09-14",
    "allocationItems": [
      {
        "taroTypeId": 1,
        "quantity": 100
      }
    ]
  }'
```

If taro type `1` has a standard price of `50.00`, omitting `pricePerKg` causes the Allocation Service to use that price automatically.

An example response is:

```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "Island Foods",
  "weekStart": "2026-09-14",
  "allocationItems": [
    {
      "id": 1,
      "taroTypeId": 1,
      "taroTypeName": "Samoan Taro",
      "quantity": 100,
      "pricePerKg": 50
    }
  ]
}
```

A custom price can instead be supplied:

```json
{
  "taroTypeId": 1,
  "quantity": 100,
  "pricePerKg": 47.50
}
```

The custom price is stored for that specific allocation item.

## Example Allocation Update

An existing allocation can be replaced using `PUT`.

```bash
curl -i -X PUT http://localhost:8082/api/v1/allocations/1 \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "weekStart": "2026-09-14",
    "allocationItems": [
      {
        "taroTypeId": 1,
        "quantity": 150,
        "pricePerKg": 48.00
      },
      {
        "taroTypeId": 2,
        "quantity": 30
      }
    ]
  }'
```

The existing allocation items are replaced by the items supplied in the update request.

If an item omits `pricePerKg`, its current standard taro price is used automatically.

Failed update requests do not partially modify the stored allocation.

## Validation

The services perform validation on incoming requests.

Examples include:

* customer name must not be blank;
* taro type name must not be blank;
* standard price cannot be negative;
* customer ID is required for an allocation;
* week start is required;
* at least one allocation item is required;
* taro type ID is required;
* quantity must be greater than zero;
* allocation item price cannot be negative;
* the same taro type cannot appear more than once in an allocation.

## Testing

The project includes automated service tests and manual end-to-end API testing.

### Customer Service Automated Tests

The Customer Service automated test suite covers:

* customer creation, retrieval, and update;
* taro type creation, retrieval, and update;
* input validation;
* duplicate handling;
* database persistence;
* concurrent duplicate customer creation;
* concurrent duplicate taro type creation.

Run the Customer Service tests with:

```bash
cd customer-service
./mvnw test
```

Current result:

```text
Tests run: 26
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

### Allocation Service Automated Tests

The Allocation Service uses a dedicated SQLite test database and mocks the remote `CustomerClient` dependency.

This allows the Allocation Service to test its own behaviour without requiring the real Customer Service to be running.

The automated tests cover:

* retrieving empty and populated allocation lists;
* retrieving allocations by ID;
* allocation creation;
* default standard pricing;
* custom allocation pricing;
* multiple allocation items;
* same customer across different weeks;
* duplicate weekly allocation handling;
* duplicate taro type handling;
* customer validation;
* taro type validation;
* request validation;
* downstream service failure handling;
* allocation updates;
* replacement allocation items;
* persistence after updates;
* failed update data integrity;
* concurrent weekly allocation creation.

Run the Allocation Service tests with:

```bash
cd allocation-service
./mvnw test
```

Current result:

```text
Tests run: 32
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

Across both services:

```text
Customer Service tests:   26
Allocation Service tests: 32
Total automated tests:    58
```

### Allocation Service Test Isolation

During automated Allocation Service testing, the real `CustomerClient` bean is replaced with a Mockito mock.

The test architecture is therefore:

```text
MockMvc
   |
   v
WeeklyAllocationController
   |
   v
WeeklyAllocationService
   |
   +----> CustomerClient (mocked)
   |
   v
Repositories
   |
   v
SQLite test database
```

The controller, service layer, repositories, validation, persistence, and exception handling remain real.

Separate manual integration tests verify the actual HTTP communication between the running Allocation Service and Customer Service.

### Concurrency Testing

The project contains concurrency tests for:

* duplicate customer creation;
* duplicate taro type creation;
* duplicate weekly allocation creation.

For the Allocation Service, two threads attempt to create the same customer/week allocation at nearly the same time.

The test verifies that:

* only one request succeeds;
* the competing request is rejected;
* only one weekly allocation remains stored.

SQLite permits only one writer at a time, so a competing simultaneous write may be rejected because the database is locked rather than reaching the normal sequential duplicate check.

### Manual Integration Testing

Manual tests run both real Spring Boot services together and verify actual HTTP communication between them.

These tests cover:

* successful weekly allocation creation;
* retrieving allocations;
* allocation updates;
* default standard pricing;
* custom weekly pricing;
* multiple taro types;
* duplicate allocations;
* duplicate taro types;
* missing customers;
* missing taro types;
* invalid request data;
* failed updates;
* downstream Customer Service failure.

Manual allocation test commands are documented in:

```text
allocation_test.md
```

## Persistence

Each service uses its own SQLite database:

```text
customer-service/customer.db
allocation-service/allocation.db
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

so application data persists between normal application restarts.

Automated tests use separate databases under each service's `target` directory so normal application data is not modified during testing.

## Development Status

The distributed backend is functional and has automated and manual test coverage.

Completed functionality includes:

* Customer Service persistence and REST API;
* customer management;
* taro type management;
* Allocation Service persistence and REST API;
* weekly allocation creation;
* weekly allocation updates;
* replacement allocation items;
* multiple allocation items;
* default and customer-specific pricing;
* service-to-service HTTP communication;
* configurable downstream service URL;
* validation and structured API errors;
* duplicate protection;
* concurrency testing;
* downstream service failure handling;
* OpenAPI contracts;
* Swagger/OpenAPI endpoint annotations;
* manual integration testing;
* automated Customer Service testing;
* automated Allocation Service testing.

Remaining work includes:

* completing the client/frontend interface;
* final end-to-end frontend testing;
* final project documentation and assessment write-up.
