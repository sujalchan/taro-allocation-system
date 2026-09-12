# Taro Allocation System — Project Goals

This document tracks the development goals for the **COMP713 Assessment 2 — Option A: Distributed Web/API Application**.

The goal is to build a small, reliable distributed application that clearly demonstrates communication between a client, multiple server-side services, and persistent data storage.

---

## 1. Core Project Goal

Build a **Taro Allocation System** for managing customers and their weekly taro allocations.

The completed system should allow a user to:

* manage customers;
* manage available taro types;
* create weekly allocations for customers;
* assign one or more taro types to an allocation;
* specify the quantity of each taro type;
* specify the actual price charged per kilogram;
* support different prices for different customers;
* view existing allocations;
* handle invalid requests appropriately.

The project should demonstrate a complete workflow:

```text
Client
   |
   | HTTP / JSON
   v
Allocation Service
   |
   | HTTP / JSON
   v
Customer Service
   |
   v
Persistent Data
```

---

# 2. Architecture Goals

## Customer Service

**Port:** `8081`

Responsibilities:

* manage customers;
* manage taro types;
* provide customer information to the Allocation Service;
* provide taro type information to the Allocation Service;
* persist customer and taro type data using SQLite.

Planned entities:

```text
Customer
TaroType
```

---

## Allocation Service

**Port:** `8082`

Responsibilities:

* manage weekly allocations;
* manage allocation items;
* communicate with the Customer Service;
* verify referenced customers and taro types;
* persist allocation data using SQLite;
* handle failures from the Customer Service appropriately.

Planned entities:

```text
WeeklyAllocation
AllocationItem
```

---

## Client

Create a simple frontend that communicates with the backend using HTTP requests and JSON.

The client should allow the user to perform the main allocation workflow without manually sending API requests.

Keep the frontend intentionally simple. The focus of the project is the distributed application and communication flow rather than complex frontend design.

---

# 3. Data Design Goals

## Customer

Planned fields:

```text
id
name
contactName
phone
active
```

A customer represents a business receiving taro.

---

## TaroType

Planned fields:

```text
id
name
description
standardPrice
```

`standardPrice` provides a default price that can be suggested when creating an allocation.

The standard price does not determine the historical price actually charged to a customer.

---

## WeeklyAllocation

Planned fields:

```text
id
customerId
weekStart
```

A Weekly Allocation represents the allocation belonging to one customer for a particular week.

A customer may have allocations across many different weeks.

---

## AllocationItem

Planned fields:

```text
id
weeklyAllocationId
taroTypeId
quantity
pricePerKg
```

A Weekly Allocation may contain multiple Allocation Items.

Each item represents a particular type of taro, its quantity, and the actual price charged to that customer.

The price is stored on the Allocation Item so that:

* different customers can receive different prices;
* a customer can receive a reduced price;
* different taro types can have different prices;
* prices can change from week to week;
* historical allocations retain the price that was actually charged.

---

# 4. Service Communication Goals

The project should clearly demonstrate communication between independent Spring Boot services.

The Allocation Service must not directly access the Customer Service database.

Instead:

```text
Allocation Service
        |
        | HTTP REST request
        v
Customer Service
        |
        | HTTP REST response
        v
Allocation Service
```

For example, when creating an allocation for:

```text
customerId = 5
```

the Allocation Service should verify the customer through the Customer Service API.

Example:

```http
GET /api/v1/customers/5
```

The response should then influence whether the allocation request succeeds or fails.

This communication should be visible and explainable during the project demonstration.

---

# 5. API Goals

Implement a small number of meaningful REST API operations.

## Customer Service

Planned operations:

```http
GET  /api/v1/customers
GET  /api/v1/customers/{id}
POST /api/v1/customers
GET  /api/v1/taro-types
```

Additional operations should only be added when they support a useful application workflow.

---

## Allocation Service

Planned operations:

```http
GET  /api/v1/allocations
GET  /api/v1/allocations/{id}
POST /api/v1/allocations
```

Optional:

```http
DELETE /api/v1/allocations/{id}
```

The goal is meaningful functionality rather than creating a large number of endpoints.

---

# 6. Validation Goals

Use Jakarta Validation and application-level checks to reject invalid data.

Examples to validate:

* customer name cannot be blank;
* quantity must be greater than zero;
* price cannot be negative;
* week/date must be valid;
* an allocation must reference an existing customer;
* an allocation item must reference an existing taro type;
* invalid IDs should not silently succeed.

Example invalid request:

```json
{
    "customerId": 2,
    "taroTypeId": 1,
    "quantity": -20,
    "pricePerKg": 6.50
}
```

Expected result:

```text
400 Bad Request
```

The client should also display useful feedback when an operation fails.

---

# 7. Error-Handling Goals

Implement clear handling for common failure cases.

At minimum demonstrate:

* invalid request data;
* resource not found;
* invalid customer;
* invalid taro type.

Where practical, also handle service communication failures.

For example:

```text
Client
   |
   | create allocation
   v
Allocation Service
   |
   | GET customer 999
   v
Customer Service
   |
   | 404 Not Found
   v
Allocation Service
   |
   | meaningful error response
   v
Client
```

Errors should return appropriate HTTP status codes and understandable responses.

---

# 8. Persistence Goals

Use **SQLite** for simple persistent storage.

Each service should own its own data.

```text
customer-service
    |
    └── SQLite
        ├── Customer
        └── TaroType


allocation-service
    |
    └── SQLite
        ├── WeeklyAllocation
        └── AllocationItem
```

Avoid directly sharing database tables between services.

Data should remain available after restarting the application where appropriate.

---

# 9. Code Quality Goals

Maintain a clear separation of responsibilities.

Use a structure similar to:

```text
controller/
service/
repository/
model/
dto/
exception/
client/
```

Responsibilities should remain separated:

```text
Controller
    |
    v
Service
    |
    +------> Service Client ------> Other Service
    |
    v
Repository
    |
    v
Database
```

Goals:

* readable class and method names;
* avoid unnecessary duplication;
* avoid hard-coded application data;
* use DTOs where useful;
* keep controllers focused on HTTP handling;
* keep business logic in services;
* keep persistence logic in repositories;
* centralise error handling where practical.

---

# 10. Testing Goals

Test important application behaviour rather than only successful requests.

Tests should cover examples such as:

* creating a valid customer;
* retrieving customers;
* creating a valid allocation;
* retrieving an allocation;
* rejecting negative quantity;
* rejecting invalid price;
* requesting a nonexistent resource;
* creating an allocation for a nonexistent customer;
* communication between the Allocation Service and Customer Service.

Keep evidence of important tests for the final report and demonstration.

---

# 11. Demonstration Goals

The final video should clearly demonstrate the complete system rather than only showing source code.

Show:

1. starting the Customer Service;
2. starting the Allocation Service;
3. starting/opening the client;
4. viewing customer data;
5. creating or modifying meaningful data;
6. creating a weekly allocation;
7. multiple taro items where appropriate;
8. customer-specific pricing;
9. data being persisted;
10. communication between services;
11. at least one invalid input;
12. at least one failed request/error-handling case.

The demonstration should make the following flow obvious:

```text
Client
  ↓
API
  ↓
Service-to-service communication
  ↓
Application logic
  ↓
Database/state change
  ↓
Response to client
```

---

# 12. README / Reproducibility Goals

The final `README.md` should contain enough information for another person to run the project.

Include:

* project description;
* architecture;
* prerequisites;
* Java version;
* Spring Boot version;
* project structure;
* database information;
* how to start Customer Service;
* how to start Allocation Service;
* how to start/use the frontend;
* API endpoint documentation;
* example requests;
* testing instructions;
* known limitations if any.

The goal is for the marker to be able to reproduce the project without guessing how it works.

---

# 13. GitHub Development Goals

Commit development regularly rather than submitting the project as a small number of large commits.

Commits should represent meaningful pieces of progress.

Example progression:

```text
initialise spring boot services

add project goals and initial documentation

add customer entity and repository

add taro type entity and repository

add customer REST API

add customer validation and error handling

add weekly allocation entity

add allocation item entity

add allocation REST API

connect allocation service to customer service

add service communication error handling

add allocation validation

add frontend customer view

add weekly allocation form

add integration tests

add run and testing instructions

update final project documentation
```

Commit messages should clearly describe what changed.

---

# 14. Weekly Progress Goals

Maintain evidence of steady development throughout the project period.

For each relevant week:

* complete relevant lab work;
* apply relevant lab concepts to the assignment;
* make meaningful GitHub commits;
* keep evidence of completed milestones;
* record major implementation or design decisions where useful.

Avoid leaving the majority of implementation until the final submission period.

---

# 15. Scope Control

The project should remain intentionally small.

## In Scope

* Spring Boot REST APIs;
* two communicating services;
* SQLite persistence;
* four main persistent entities;
* simple frontend;
* CRUD or meaningful allocation workflows;
* HTTP/JSON communication;
* validation;
* error handling;
* automated testing;
* clear documentation.

## Out of Scope Unless Required Later

Avoid adding unnecessary complexity such as:

* complex authentication;
* JWT authentication;
* user account systems;
* Kafka;
* Redis;
* Kubernetes;
* service discovery;
* advanced cloud deployment;
* large database schemas;
* large frontend frameworks purely for complexity.

Features should only be added when they meaningfully improve the assignment requirements or demonstration.

---

# 16. Development Checklist

## Initial Setup

* [ ] Create GitHub repository
* [ ] Create root project directory
* [ ] Create `customer-service`
* [ ] Create `allocation-service`
* [ ] Configure Java 21
* [ ] Configure service ports
* [ ] Configure SQLite
* [ ] Add `.gitignore`
* [ ] Add `README.md`
* [ ] Add `GOALS.md`
* [ ] Verify both Spring Boot applications start
* [ ] Make initial GitHub commits

## Customer Service

* [ ] Create `Customer` entity
* [ ] Create `TaroType` entity
* [ ] Create repositories
* [ ] Create service layer
* [ ] Create REST controllers
* [ ] Add validation
* [ ] Add error handling
* [ ] Test Customer Service endpoints

## Allocation Service

* [ ] Create `WeeklyAllocation` entity
* [ ] Create `AllocationItem` entity
* [ ] Create repositories
* [ ] Create service layer
* [ ] Create REST controllers
* [ ] Add validation
* [ ] Add error handling
* [ ] Test Allocation Service endpoints

## Distributed Communication

* [ ] Create Customer Service API client
* [ ] Allocation Service can retrieve customer information
* [ ] Allocation Service can verify customer IDs
* [ ] Allocation Service can verify taro type IDs
* [ ] Handle `404` responses from Customer Service
* [ ] Handle unavailable Customer Service where practical
* [ ] Demonstrate JSON request/response communication

## Frontend

* [ ] Create basic frontend
* [ ] Display customers
* [ ] Display taro types
* [ ] Create weekly allocation
* [ ] Add multiple allocation items
* [ ] Allow price to be changed from standard price
* [ ] Display existing allocations
* [ ] Display validation/error messages

## Testing

* [ ] Unit/service tests
* [ ] API tests
* [ ] Validation tests
* [ ] Not-found test
* [ ] Invalid allocation test
* [ ] Service communication test
* [ ] Persistence test
* [ ] Record useful testing evidence

## Final Submission

* [ ] Clean up code
* [ ] Remove unused code
* [ ] Confirm both services build
* [ ] Confirm clean startup
* [ ] Confirm database creation/setup
* [ ] Confirm complete workflow works
* [ ] Confirm invalid-input workflow works
* [ ] Update README
* [ ] Document API endpoints
* [ ] Add setup instructions
* [ ] Add testing instructions
* [ ] Complete technical report
* [ ] Record demonstration video
* [ ] Verify submitted project opens correctly
* [ ] Clearly identify any incomplete functionality
* [ ] Review GitHub commit history
* [ ] Confirm all submission files are accessible
