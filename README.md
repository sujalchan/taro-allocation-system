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

The Allocation Service stores only the IDs of customers and taro types that are owned by the Customer Service.

When an allocation is requested, the Allocation Service communicates with the Customer Service to validate those IDs and retrieve information such as customer and taro type names.

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

Customer names are protected by a database uniqueness constraint to prevent duplicate customers, including during concurrent requests.

### TaroType

Stores the different types of taro available.

Fields include:

* `id`
* `name`
* `description`
* `standardPrice`

The standard price acts as the default price when creating a new allocation item.

### WeeklyAllocation

Represents a customer's allocation for a particular week.

Fields include:

* `id`
* `customerId`
* `weekStart`

A customer can only have one allocation for a particular week.

The database contains a unique constraint on:

```text
customerId + weekStart
```

to protect against duplicate allocations.

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

If `pricePerKg` is omitted when creating an allocation, the current `standardPrice` from the Customer Service is used automatically.

The same taro type cannot appear more than once inside a single weekly allocation.

## Technologies

* Java 21
* Spring Boot 4
* Spring Web
* Spring REST Client
* Spring Data JPA
* Jakarta Validation
* Hibernate
* SQLite
* Maven
* OpenAPI

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
* preventing duplicate customer names.

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
* preventing duplicate taro type names.

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
```

Supported operations include:

* retrieving all weekly allocations;
* retrieving a weekly allocation by ID;
* creating weekly allocations;
* assigning multiple taro types to an allocation;
* using customer-specific weekly prices;
* automatically using the standard taro price when no custom price is supplied;
* preventing duplicate taro types inside an allocation;
* preventing duplicate allocations for the same customer and week;
* validating customer and taro type IDs through the Customer Service.

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

This is used to:

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
| `400`  | Validation errors                        |
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

with a response such as:

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

The custom price will then be stored for that allocation.

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

The project includes both automated and manual API testing.

Customer Service tests cover:

* customer CRUD operations;
* taro type CRUD operations;
* input validation;
* duplicate handling;
* database persistence;
* concurrent duplicate creation.

Allocation Service has been manually tested for:

* creating weekly allocations;
* retrieving allocations;
* default standard pricing;
* custom weekly pricing;
* multiple taro types per allocation;
* duplicate weekly allocations;
* duplicate taro types;
* missing customers;
* missing taro types;
* invalid request data;
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

## Development Status

The backend distributed services are currently functional.

Completed functionality includes:

* Customer Service persistence and REST API;
* customer management;
* taro type management;
* Allocation Service persistence and REST API;
* weekly allocation creation;
* multiple allocation items;
* default and customer-specific pricing;
* service-to-service HTTP communication;
* validation and structured API errors;
* duplicate protection;
* downstream service failure handling;
* OpenAPI contracts;
* manual integration testing;
* automated Customer Service testing.

Remaining work includes:

* completing the client/frontend interface;
* expanding automated tests for the Allocation Service;
* final project documentation and assessment write-up.
