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
--header 'Content-Type: application/json' \
--data '{
"id": "70e1504c-4aaf-40cf-975e-3b1f6777acc3",
"accountId": "dba7eb37-1d9c-41b8-912b-20259d087815",
"symbol": "TSLA",
"side": "BUY",
"quantity": 1
}'
curl --location 'http://localhost:8084/orders?accountId=1'

curl --location 'http://localhost:8084/orders/dba7eb37-1d9c-41b8-912b-20259d087815'