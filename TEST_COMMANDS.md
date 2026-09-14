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