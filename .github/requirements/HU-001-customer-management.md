# HU-001: Customer and Person Management

## Title

Customer and Person Management

## User Story

As a system administrator, I want to perform CRUD operations on customers, to maintain a registry of bank users.

## Acceptance Criteria

### Domain: Person Entity (Pure Domain)

**Scenario: Create a person with all fields**
**Given** the system requires a new person
**When** I provide name, gender, age, identification, address and phone
**Then** the person entity is created with all specified fields
**And** the system generates a unique identifier for the person

**Scenario: Person identification must be unique**
**Given** a person with identification "12345678" already exists
**When** I try to create another person with the same identification
**Then** the system returns a validation error
**And** the error message indicates the identification is already in use

### Domain: Customer Entity

**Scenario: Customer inherits from Person**
**Given** a person exists in the system
**When** I create a customer associated with that person
**Then** the customer inherits all Person fields
**And** the customer includes additional fields: clientId, password and status

**Scenario: Create customer with business fields**
**Given** a person with id "1" exists
**When** I create a customer with clientId "C001", password "secret123" and status "true"
**Then** the customer is created successfully
**And** the customer is linked to the person with id "1"

### Persistence

**Scenario: Unique identification constraint**
**Given** the database contains a person with identification "12345678"
**When** attempting to save another person with the same identification
**Then** the database raises a unique constraint violation
**And** the operation is rejected by the persistence layer

### API Endpoints

**Scenario: GET /clientes returns all customers**
**Given** there are customers in the system
**When** I send a GET request to `/clientes`
**Then** the system returns a list of all customers
**And** each customer includes person data, clientId, password and status

**Scenario: POST /clientes creates a new customer**
**Given** valid customer data is provided
**When** I send a POST request to `/clientes` with the customer information
**Then** the system creates a new customer
**And** returns the created customer with status 201

**Scenario: PUT /clientes updates an existing customer**
**Given** a customer with id "1" exists
**When** I send a PUT request to `/clientes/1` with updated data
**Then** the system updates the customer information
**And** returns the updated customer

**Scenario: PATCH /clientes partially updates a customer**
**Given** a customer with id "1" exists
**When** I send a PATCH request to `/clientes/1` with partial data
**Then** the system updates only the provided fields
**And** preserves the unchanged fields

**Scenario: DELETE /clientes removes a customer**
**Given** a customer with id "1" exists
**When** I send a DELETE request to `/clientes/1`
**Then** the system removes the customer
**And** returns a success confirmation

### Seniority (Async): Event Emission

**Scenario: Emit event when customer is created**
**Given** a valid customer is created via POST
**When** the customer is successfully persisted
**Then** the system emits a "cliente.creado" event to RabbitMQ
**And** the event contains the customerId and relevant data

**Scenario: Event not emitted on failed creation**
**Given** an invalid customer payload is submitted
**When** the creation fails due to validation errors
**Then** no event is emitted to RabbitMQ
**And** the system returns appropriate error response