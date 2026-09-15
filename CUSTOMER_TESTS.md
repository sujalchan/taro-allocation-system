# run customer-service
cd customer-service
./mvnw spring-boot:run

# return customers
curl -i http://localhost:8081/api/v1/customers

# create customer / test duplicate
curl -i -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Island Foods",
    "contactName": "John",
    "phone": "0211234567",
    "active": true
  }'

# return first customer
curl -i http://localhost:8081/api/v1/customers/1

# invalid customer test
curl -i http://localhost:8081/api/v1/customers/999

# validation test
curl -i -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "contactName": "Sarah",
    "phone": "0219876543",
    "active": true
  }'

### PUT tests ###

# update record for 'Island Foods'
curl -i -X PUT http://localhost:8081/api/v1/customers/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Island Foods",
    "contactName": "James",
    "phone": "0219999999",
    "active": true
  }'

# return updated customer
curl -i http://localhost:8081/api/v1/customers/1

# update missing customer
curl -i -X PUT http://localhost:8081/api/v1/customers/999 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Customer",
    "contactName": "John",
    "phone": "0210000000",
    "active": true
  }'

# validation test
curl -i -X PUT http://localhost:8081/api/v1/customers/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "contactName": "James",
    "phone": "0219999999",
    "active": true
  }'

# test 409 (Customer Already Exists)
curl -i -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fresh Choice",
    "contactName": "Sarah",
    "phone": "0215555555",
    "active": true
  }'

curl -i -X PUT http://localhost:8081/api/v1/customers/2 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Island Foods",
    "contactName": "Sarah",
    "phone": "0215555555",
    "active": true
  }'

### TARO_TESTS ###

# return taro types

curl -i http://localhost:8081/api/v1/taro-types


# create taro type

curl -i -X POST http://localhost:8081/api/v1/taro-types \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Samoan Taro",
    "description": "Large premium taro",
    "standardPrice": 50.00
  }'


# test duplicate taro type

curl -i -X POST http://localhost:8081/api/v1/taro-types \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Samoan Taro",
    "description": "Large premium taro",
    "standardPrice": 50.00
  }'


# return first taro type

curl -i http://localhost:8081/api/v1/taro-types/1


# invalid taro type test

curl -i http://localhost:8081/api/v1/taro-types/999


# validation test - blank name

curl -i -X POST http://localhost:8081/api/v1/taro-types \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "description": "Invalid taro type",
    "standardPrice": 50.00
  }'


# validation test - missing price

curl -i -X POST http://localhost:8081/api/v1/taro-types \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Taro",
    "description": "Taro without a price"
  }'


# validation test - negative price

curl -i -X POST http://localhost:8081/api/v1/taro-types \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Taro",
    "description": "Taro with an invalid price",
    "standardPrice": -10.00
  }'


### PUT tests ###


# update record for 'Samoan Taro'

curl -i -X PUT http://localhost:8081/api/v1/taro-types/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Samoan Taro",
    "description": "Updated premium Samoan taro",
    "standardPrice": 55.00
  }'


# return updated taro type

curl -i http://localhost:8081/api/v1/taro-types/1


# update missing taro type

curl -i -X PUT http://localhost:8081/api/v1/taro-types/999 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Taro",
    "description": "Test description",
    "standardPrice": 40.00
  }'


# validation test - blank name

curl -i -X PUT http://localhost:8081/api/v1/taro-types/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "description": "Invalid update",
    "standardPrice": 50.00
  }'


# validation test - negative price

curl -i -X PUT http://localhost:8081/api/v1/taro-types/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Samoan Taro",
    "description": "Invalid price update",
    "standardPrice": -10.00
  }'


# create second taro type for duplicate update test

curl -i -X POST http://localhost:8081/api/v1/taro-types \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fiji Taro",
    "description": "Fijian taro",
    "standardPrice": 45.00
  }'


# test 409 - rename Fiji Taro to existing Samoan Taro

curl -i -X PUT http://localhost:8081/api/v1/taro-types/2 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Samoan Taro",
    "description": "Fijian taro",
    "standardPrice": 45.00
  }'