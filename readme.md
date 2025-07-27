# Order Service

A Spring Boot application for managing orders.

## Features

- RESTful API for order management
- SQL database integration
- DTOs with fixed precision for monetary values

## Technologies

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Docker
- resilience4j for rate limiting

## Setup

1. Clone the repository
2. Run docker compose 
3. Interact with the api using Postman or curl

## Running the Application
navigate cloned app
run docker-compose --env-file .env up --build

## CURL
curl --location 'http://localhost:8084/orders' \
--header 'X-Idempotency-Key: 7bf8c6b4-9a4a-445c-be69-f06abe708aa9' \
--header 'Content-Type: application/json' \
--data '{
"id": "70e1504c-4aaf-40cf-975e-3b1f6777acc3",
"accountId": "dba7eb37-1d9c-41b8-912b-20259d087815",
"symbol": "AAPL",
"side": "BUY",
"quantity": 1
}'

curl --location 'http://localhost:8084/orders' \
--header 'X-Idempotency-Key: d26c4583-84c2-45c4-83fd-d4167d127e61' \
--header 'Content-Type: application/json' \
--data '{
"id": "70e1504c-4aaf-40cf-975e-3b1f6777acc3",
"accountId": "dba7eb37-1d9c-41b8-912b-20259d087815",
"symbol": "error",
"side": "BUY",
"quantity": 1
}'

curl --location 'http://localhost:8084/orders/7a00fabe-45a1-46aa-9b45-698ae4caa904'

curl --location 'http://localhost:8084/orders?accountId=dba7eb37-1d9c-41b8-912b-20259d087815'

### Considerations

1. For status fail of an order -> I have chosen  to fail if price is null in feed response, it is a showcase not an actual possibility but specs were not really clear on this
2. For response statuses of 422 and 503 -> I have chosen  to send error message in body, it is a showcase not an actual possibility but specs were not really clear on this
3. Ids exposed in the API are UUIDs, database primary keys are hidden from the caller and are not exposed in the API. 