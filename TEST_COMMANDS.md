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