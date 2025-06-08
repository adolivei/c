# Calculator API

A distributed calculator service built with Spring Boot and Apache Kafka that provides basic arithmetic operations with arbitrary precision decimal numbers.

## Features

- RESTful API for basic arithmetic operations (sum, subtraction, multiplication, division)
- Support for arbitrary precision signed decimal numbers using BigDecimal
- Distributed architecture using Apache Kafka for inter-service communication
- End-to-end request tracing via correlation ID (MDC propagation)
- Docker support for easy deployment

## Prerequisites

- Java 21 or later
- Maven 3.8 or later
- Docker and Docker Compose (for containerized deployment)

## Project Structure

The project consists of three modules:
- `contract`: Defines the data transfer objects (DTOs) for communication between services
- `calculator`: Core calculation service that processes arithmetic operations
- `rest`: REST API service that exposes endpoints and communicates with the calculator service

## Building the Project

To build the project locally, run the following command from the root directory:

```bash
./mvnw clean install
```

This will build all three modules and run the unit tests.

## Running Locally

1. Start Kafka and Zookeeper using Docker Compose:
```bash
docker-compose up -d zookeeper kafka
```

2. Start the calculator service in a new terminal:
```bash
./mvnw spring-boot:run --projects calculator
```

3. Start the REST service in another new terminal:
```bash
./mvnw spring-boot:run --projects rest
```

The REST API will be available at `http://localhost:8080`

## Running with Docker

To run the entire application using Docker:

```bash
docker-compose up --build
```

This will start all services:
- Zookeeper
- Kafka
- Calculator service
- REST API service

## API Usage

### Calculate Operation

Send a POST request to `/calculate` with the following JSON payload:

```http
POST /calculate
Content-Type: application/json

{
    "a": "10.5",
    "b": "2.5",
    "operation": "SUM"
}
```

Available operations:
- `SUM`
- `SUBTRACTION`
- `MULTIPLICATION`
- `DIVISION`

Example response:
```json
{
    "result": "13.0"
}
```

In case of an error, the response will be in the following format:
```json
{
    "error": "Error message"
}
```

## Testing

To run the unit tests for all modules:

```bash
./mvnw test
```

The project includes comprehensive unit tests for:
- Calculator operations (sum, subtraction, multiplication, division)
- Request/response handling
- Error cases
- Kafka message processing
- Input validation

## Error Handling

The API handles the following error cases:
- Division by zero
- Invalid operation
- Invalid input numbers
- Timeout waiting for calculation response
- Internal server errors

All errors are properly logged with correlation IDs for tracing.

## Configuration

The application can be configured through `application.properties` files in each module. Key configurations:

- Kafka bootstrap servers
- Kafka topics
- Consumer group IDs
- Request timeout (for REST service)
- Operation-specific settings (e.g., division scale)

For Docker deployment, use `application-docker.properties` which contains the appropriate Kafka configuration for containerized environment.

## Logging

The application uses SLF4J with Logback for logging. All log lines related to a specific HTTP request or Kafka message automatically include a `correlationId` (request identifier) using MDC (Mapped Diagnostic Context) propagation. This enables end-to-end tracing of requests across all modules and services.

**Example log line:**
```
2024-06-01 12:34:56 [http-nio-8080-exec-1] INFO  c.e.c.control.CalculatorController - [b2f1c8e2-...-a1d3] Received calculation request: 10.5 SUM 2.5
```

The `correlationId` is generated at the entry point of each HTTP request and is propagated through Kafka messages and all internal processing. This value is included in every log line for that request, making it easy to trace the full lifecycle of a request across distributed modules.
