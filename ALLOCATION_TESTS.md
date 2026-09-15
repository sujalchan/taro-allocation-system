# Allocation Service Manual Test Commands

This file contains the `curl` commands used to reproduce the manual tests performed against `customer-service` and `allocation-service`.

Assumptions:

- `customer-service` runs on `http://localhost:8081`
- `allocation-service` runs on `http://localhost:8082`
- both databases are fresh before starting (delete customer.db and allocation.db)

- commands are run in the order shown so IDs are predictable

---

## 1. Confirm customer-service starts empty

### Return all customers

```bash
curl -i http://localhost:8081/api/v1/customers
```

Expected:

```text
HTTP/1.1 200
[]
```

### Return all taro types

```bash
curl -i http://localhost:8081/api/v1/taro-types
```

Expected:

```text
HTTP/1.1 200
[]
```

---

## 2. Seed customer data

### Create customer 1 - Island Foods

```bash
curl -i -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Island Foods",
    "contactName": "John",
    "phone": "0211234567",
    "active": true
  }'
```

Expected:

```text
HTTP/1.1 201
```

### Create customer 2 - Fresh Choice

```bash
curl -i -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fresh Choice",
    "contactName": "Sarah",
    "phone": "0215555555",
    "active": true
  }'
```

Expected:

```text
HTTP/1.1 201
```

### Confirm seeded customers

```bash
curl -i http://localhost:8081/api/v1/customers
```

---

## 3. Seed taro type data

### Create taro type 1 - Samoan Taro

```bash
curl -i -X POST http://localhost:8081/api/v1/taro-types \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Samoan Taro",
    "description": "Large premium taro",
    "standardPrice": 50.00
  }'
```

Expected:

```text
HTTP/1.1 201
```

### Create taro type 2 - Fiji Taro

```bash
curl -i -X POST http://localhost:8081/api/v1/taro-types \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fiji Taro",
    "description": "Fijian taro",
    "standardPrice": 45.00
  }'
```

Expected:

```text
HTTP/1.1 201
```

### Confirm seeded taro types

```bash
curl -i http://localhost:8081/api/v1/taro-types
```

---

# Allocation Service Tests

## 4. Return all allocations from a fresh database

```bash
curl -i http://localhost:8082/api/v1/allocations
```

Expected:

```text
HTTP/1.1 200
[]
```

---

## 5. Create allocation using default standard price

No `pricePerKg` is supplied, so Samoan Taro should use its standard price of `50.00`.

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

Expected:

```text
HTTP/1.1 201
```

The response should contain:

```text
customerName = Island Foods
taroTypeName = Samoan Taro
pricePerKg = 50
```

---

## 6. Create allocation using custom prices

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 2,
    "weekStart": "2026-09-14",
    "allocationItems": [
      {
        "taroTypeId": 1,
        "quantity": 80,
        "pricePerKg": 47.50
      },
      {
        "taroTypeId": 2,
        "quantity": 40,
        "pricePerKg": 42.50
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 201
```

Expected prices:

```text
Samoan Taro -> 47.50
Fiji Taro   -> 42.50
```

---

## 7. Same customer on a different week

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "weekStart": "2026-09-21",
    "allocationItems": [
      {
        "taroTypeId": 2,
        "quantity": 60
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 201
```

Because no custom price is supplied, Fiji Taro should use `45.00`.

---

## 8. Return all allocations

```bash
curl -i http://localhost:8082/api/v1/allocations
```

Expected:

```text
HTTP/1.1 200
```

Three allocations should be returned.

---

## 9. Return allocation 1

```bash
curl -i http://localhost:8082/api/v1/allocations/1
```

Expected:

```text
HTTP/1.1 200
```

---

## 10. Missing allocation test

```bash
curl -i http://localhost:8082/api/v1/allocations/999
```

Expected:

```text
HTTP/1.1 404
```

Expected error code:

```text
WEEKLY_ALLOCATION_NOT_FOUND
```

---

# Conflict and Duplicate Tests

## 11. Duplicate weekly allocation

Customer `1` already has an allocation for `2026-09-14`.

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "weekStart": "2026-09-14",
    "allocationItems": [
      {
        "taroTypeId": 2,
        "quantity": 20
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 409
```

Expected error code:

```text
WEEKLY_ALLOCATION_ALREADY_EXISTS
```

---

## 12. Duplicate taro type in one allocation

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "weekStart": "2026-09-28",
    "allocationItems": [
      {
        "taroTypeId": 1,
        "quantity": 100
      },
      {
        "taroTypeId": 1,
        "quantity": 50
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 400
```

Expected error code:

```text
DUPLICATE_TARO_TYPE
```

---

# Remote Resource Validation Tests

## 13. Missing customer

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 999,
    "weekStart": "2026-09-28",
    "allocationItems": [
      {
        "taroTypeId": 1,
        "quantity": 100
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 404
```

Expected error code:

```text
CUSTOMER_NOT_FOUND
```

---

## 14. Missing taro type

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "weekStart": "2026-09-28",
    "allocationItems": [
      {
        "taroTypeId": 999,
        "quantity": 100
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 404
```

Expected error code:

```text
TARO_TYPE_NOT_FOUND
```

---

# Request Validation Tests

## 15. Missing customer ID

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "weekStart": "2026-09-28",
    "allocationItems": [
      {
        "taroTypeId": 1,
        "quantity": 100
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 400
```

Expected message:

```text
Customer ID is required
```

---

## 16. Missing week start

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "allocationItems": [
      {
        "taroTypeId": 1,
        "quantity": 100
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 400
```

Expected message:

```text
Week start is required
```

---

## 17. Empty allocation item list

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "weekStart": "2026-09-28",
    "allocationItems": []
  }'
```

Expected:

```text
HTTP/1.1 400
```

Expected message:

```text
At least one allocation item is required
```

---

## 18. Missing taro type ID

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "weekStart": "2026-09-28",
    "allocationItems": [
      {
        "quantity": 100
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 400
```

Expected message:

```text
Taro type ID is required
```

---

## 19. Quantity equals zero

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "weekStart": "2026-09-28",
    "allocationItems": [
      {
        "taroTypeId": 1,
        "quantity": 0
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 400
```

Expected message:

```text
Quantity must be greater than zero
```

---

## 20. Negative quantity

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "weekStart": "2026-09-28",
    "allocationItems": [
      {
        "taroTypeId": 1,
        "quantity": -50
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 400
```

Expected message:

```text
Quantity must be greater than zero
```

---

## 21. Negative price per kg

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "weekStart": "2026-09-28",
    "allocationItems": [
      {
        "taroTypeId": 1,
        "quantity": 100,
        "pricePerKg": -5.00
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 400
```

Expected message:

```text
Price per kg cannot be negative
```

---

# Customer Service Unavailable Tests

For the following tests, stop `customer-service` while leaving `allocation-service` running.

## 22. GET allocation while customer-service is unavailable

```bash
curl -i http://localhost:8082/api/v1/allocations/1
```

Expected:

```text
HTTP/1.1 503
```

Expected response:

```json
{
  "code": "CUSTOMER_SERVICE_UNAVAILABLE",
  "message": "Customer service is currently unavailable",
  "path": "/api/v1/allocations/1"
}
```

---

## 23. POST allocation while customer-service is unavailable

```bash
curl -i -X POST http://localhost:8082/api/v1/allocations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "weekStart": "2026-10-05",
    "allocationItems": [
      {
        "taroTypeId": 1,
        "quantity": 100
      }
    ]
  }'
```

Expected:

```text
HTTP/1.1 503
```

Expected response:

```json
{
  "code": "CUSTOMER_SERVICE_UNAVAILABLE",
  "message": "Customer service is currently unavailable",
  "path": "/api/v1/allocations"
}
```

---

# Summary

These commands reproduce the manual tests for:

- empty database responses
- customer and taro type seed data
- successful weekly allocation creation
- default taro pricing
- custom weekly pricing
- retrieving all allocations
- retrieving a single allocation
- missing allocation handling
- duplicate weekly allocation handling
- duplicate taro type handling
- missing customer handling
- missing taro type handling
- request validation
- downstream `customer-service` failure handling
