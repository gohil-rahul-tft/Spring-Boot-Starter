# Todo API Documentation

This is a Spring Boot CRUD application with in-memory storage for todos. All responses follow a consistent API wrapper format and proper error handling.

## Base URL
`http://localhost:8082`

## Authentication
This API uses JWT for authentication. To access protected endpoints, you need to include a JWT token in the `Authorization` header of your request.

`Authorization: Bearer <your_jwt_token>`

### 1. Register
- **POST** `/api/auth/register`
- **Description**: Register a new user.
- **Content-Type**: `application/json`
- **Request Body**:
  ```json
  {
    "username": "string (required)",
    "password": "string (required)"
  }
  ```
- **Response**: `200 OK` with a success message, or `400 Bad Request` if the username is already taken.

### 2. Login
- **POST** `/api/auth/login`
- **Description**: Authenticate a user and get a JWT token.
- **Content-Type**: `application/json`
- **Request Body**:
  ```json
  {
    "username": "string (required)",
    "password": "string (required)"
  }
  ```
- **Response**: `200 OK` with `AuthResponse` containing the JWT token.

## Response Format
All API responses follow this wrapper format:
```json
{
  "status": true/false,
  "message": "Success or error message",
  "data": {} or [] (null for errors or delete operations)
}
```

## HTTP Status Codes
- `200 OK` - Successful GET, PUT, PATCH and DELETE requests
- `201 Created` - Successful POST request
- `400 Bad Request` - Invalid request parameters (negative IDs, etc.)
- `401 Unauthorized` - Authentication failed
- `404 Not Found` - Resource not found
- `422 Unprocessable Entity` - Validation errors
- `500 Internal Server Error` - Server errors

## Endpoints

### 3. Get All Todos
- **GET** `/api/todos`
- **Description**: Retrieve all todos
- **Response**: `200 OK` with `ApiResponse<List<TodoResponse>>`

### 4. Get Todo by ID
- **GET** `/api/todos/{id}`
- **Description**: Retrieve a specific todo by ID
- **Response**: `200 OK` with `ApiResponse<TodoResponse>`, or `404 Not Found` if todo doesn't exist

### 5. Create Todo
- **POST** `/api/todos`
- **Content-Type**: `application/json`
- **Request Body**:
  ```json
  {
    "title": "string (required, max 255 chars)",
    "description": "string (optional, max 1000 chars)",
    "completed": "boolean (optional, default: false)"
  }
  ```
- **Response**: `201 Created` with `ApiResponse<TodoResponse>`
- **Validation**: Returns `422 Unprocessable Entity` for validation errors

### 6. Update Todo
- **PUT** `/api/todos/{id}`
- **Content-Type**: `application/json`
- **Request Body**:
  ```json
  {
    "title": "string (required, max 255 chars)",
    "description": "string (optional, max 1000 chars)",
    "completed": "boolean (optional)"
  }
  ```
- **Response**: `200 OK` with `ApiResponse<TodoResponse>`, or `404 Not Found` if todo doesn't exist
- **Validation**: Returns `422 Unprocessable Entity` for validation errors

### 7. Partial Update Todo
- **PATCH** `/api/todos/{id}`
- **Content-Type**: `application/json`
- **Request Body** (only specify fields to update):
  ```json
  {
    "title": "string (optional, max 255 chars)",
    "description": "string (optional, max 1000 chars)",
    "completed": "boolean (optional)"
  }
  ```
- **Response**: `200 OK` with `ApiResponse<TodoResponse>`, or `404 Not Found` if todo doesn't exist
- **Validation**: Returns `422 Unprocessable Entity` for validation errors

### 8. Delete Todo
- **DELETE** `/api/todos/{id}`
- **Description**: Delete a specific todo by ID
- **Response**: `200 OK` with `ApiResponse<Unit>`, or `404 Not Found` if todo doesn't exist

### 9. Delete All Todos
- **DELETE** `/api/todos`
- **Description**: Delete all todos
- **Response**: `200 OK` with `ApiResponse<Unit>`

## Validation Rules
- **Title**: Required, cannot be blank, max 255 characters
- **Description**: Optional, max 1000 characters
- **ID**: Must be a positive number (> 0)

## Example Usage

### Register a new user:
```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }'
```

### Login:
```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }'
```

### Create a new todo:
```bash
curl -X POST http://localhost:8082/api/todos \
  -H "Authorization: Bearer <your_jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Learn Spring Boot",
    "description": "Build a todo application",
    "completed": false
  }'
```

### Get all todos:
```bash
curl -X GET http://localhost:8082/api/todos \
  -H "Authorization: Bearer <your_jwt_token>"
```

### Update a todo:
```bash
curl -X PUT http://localhost:8082/api/todos/1 \
  -H "Authorization: Bearer <your_jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Learn Spring Boot with Kotlin",
    "description": "Build a more complex todo application",
    "completed": true
  }'
```

### Delete a todo:
```bash
curl -X DELETE http://localhost:8082/api/todos/1 \
  -H "Authorization: Bearer <your_jwt_token>"
```

### Example Response Format:
```json
{
  "status": true,
  "message": "Todos retrieved successfully",
  "data": [
    {
      "id": 1,
      "title": "Learn Spring Boot",
      "description": "Build a todo application",
      "completed": false,
      "createdAt": "2025-11-15T20:32:07.3996801",
      "updatedAt": "2025-11-15T20:32:07.3996801"
    }
  ]
}
```

### Example Error Response Format:
```json
{
  "status": false,
  "message": "Todo with ID 999 not found",
  "data": null
}
```