# Taro Allocation System

A distributed web/API application for managing weekly taro allocations for customers.

This project is being developed for **COMP713 – Distributed Systems, Assessment 2 (Option A)**.

## Overview

The Taro Allocation System is designed to manage customers, taro types, and weekly taro allocations.

The application is split into two Spring Boot services:

* **Customer Service** – manages customers and taro types.
* **Allocation Service** – manages weekly allocations and allocation items.

The services communicate using HTTP REST requests.

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

Each service manages its own SQLite database.

```text
customer-service
├── Customer
└── TaroType

allocation-service
├── WeeklyAllocation
└── AllocationItem
```

## Data Model

### Customer

Stores information about businesses receiving taro.

Example fields:

* `id`
* `name`
* `contactName`
* `phone`
* `active`

### TaroType

Stores the different types of taro available.

Example fields:

* `id`
* `name`
* `description`
* `standardPrice`

### WeeklyAllocation

Represents a customer's allocation for a particular week.

Example fields:

* `id`
* `customerId`
* `weekStart`

### AllocationItem

Represents a particular taro type within a weekly allocation.

Example fields:

* `id`
* `weeklyAllocationId`
* `taroTypeId`
* `quantity`
* `pricePerKg`

The price is stored on each allocation item so that different customers can receive different prices and prices can change between weeks.

## Technologies

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Jakarta Validation
* SQLite
* Maven

## Project Structure

```text
taro-allocation-system/
├── customer-service/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── allocation-service/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── frontend/
│
└── README.md
```

## Running the Application

### Customer Service

```bash
cd customer-service
./mvnw spring-boot:run
```

The Customer Service will run on:

```text
http://localhost:8081
```

### Allocation Service

Open another terminal:

```bash
cd allocation-service
./mvnw spring-boot:run
```

The Allocation Service will run on:

```text
http://localhost:8082
```

## Planned Functionality

The application will support functionality such as:

* viewing customers;
* adding customers;
* viewing available taro types;
* creating weekly allocations;
* assigning multiple taro types to a weekly allocation;
* setting customer-specific prices for allocation items;
* viewing existing weekly allocations;
* validating incoming requests;
* handling invalid or failed requests;
* communication between the Allocation Service and Customer Service.

## Development Status

The project is currently under development.
