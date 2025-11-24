# Spring Boot Todo Application

This is a Spring Boot application with full CRUD operations for managing todos. It uses an in-memory data store for simplicity and includes proper error handling, validation, and API response wrappers. This project also includes features like JWT-based authentication, user management, and CSV file processing.

## Features

- Full CRUD operations for todos
- RESTful API endpoints with consistent response format
- Proper validation with 422 status codes for validation errors
- Error handling with appropriate HTTP status codes (400, 404, 422, 500)
- API response wrapper: `{status: true/false, message: "message", data: {}/[]}`
- Clean code architecture with separation of concerns
- Unit and integration tests
- **Authentication & Authorization**: JWT-based security for accessing protected endpoints.
- **User Management**: Endpoints for user registration and login.
- **CSV Processing**: Multiple endpoints for uploading and processing CSV files, including single record, batch, and Spring Batch-based methods.

## Architecture

The application follows a clean architecture pattern with the following layers:

- **Controller Layer**: Handles HTTP requests, validation, and API responses.
- **Service Layer**: Contains business logic.
- **Repository Layer**: Handles data persistence.
- **Entity/Model Layer**: Represents the data structure.
- **DTO Layer**: Data transfer objects for API requests/responses.
- **Security**: Handles authentication and authorization using JWT.
- **Common Layer**: Shared utilities like API response wrapper.

## Project Structure

```
Spring-Boot-Starter/
├── src/
│   ├── main/
│   │   ├── kotlin/com/world/spring/
│   │   │   ├── core/                          # Core infrastructure components
│   │   │   │   ├── config/                    # Application configuration
│   │   │   │   │   └── DataInitializer.kt     # Initializes default admin user on startup
│   │   │   │   └── security/                  # Security infrastructure
│   │   │   │       ├── config/
│   │   │   │       │   ├── SecurityConfig.kt  # Spring Security configuration
│   │   │   │       │   └── WebConfig.kt       # Web MVC configuration
│   │   │   │       ├── jwt/
│   │   │   │       │   ├── JwtAuthFilter.kt   # JWT authentication filter
│   │   │   │       │   └── JwtTokenProvider.kt # JWT token generation and validation
│   │   │   │       ├── service/
│   │   │   │       │   └── LocalUserDetailsService.kt # User details service for authentication
│   │   │   │       └── CustomAuthEntryPoint.kt # Custom authentication entry point
│   │   │   │
│   │   │   ├── features/                      # Feature modules (domain-driven design)
│   │   │   │   ├── auth/                      # Authentication and user management
│   │   │   │   │   ├── controller/
│   │   │   │   │   │   └── AuthController.kt  # Login and registration endpoints
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   └── UserEntity.kt      # User entity with JPA annotations
│   │   │   │   │   ├── enums/
│   │   │   │   │   │   └── RoleEnum.kt        # User roles (USER, ADMIN)
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── UserRepository.kt  # User data access
│   │   │   │   │   ├── request/
│   │   │   │   │   │   ├── AuthRequest.kt     # Login request DTO
│   │   │   │   │   │   └── RegisterRequest.kt # Registration request DTO
│   │   │   │   │   ├── response/
│   │   │   │   │   │   └── AuthResponse.kt    # Authentication response with JWT token
│   │   │   │   │   └── service/
│   │   │   │   │       └── UserService.kt     # User business logic
│   │   │   │   │
│   │   │   │   ├── new_csv/                   # Spring Batch CSV processing
│   │   │   │   │   ├── config/
│   │   │   │   │   │   ├── BatchConfig.kt     # Spring Batch job configuration
│   │   │   │   │   │   └── CsvPersonProcessor.kt # Item processor for batch jobs
│   │   │   │   │   ├── controller/
│   │   │   │   │   │   └── CsvController.kt   # CSV upload endpoints
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   └── CsvPerson.kt       # CSV person entity
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── CsvPersonRepository.kt # CSV person data access
│   │   │   │   │   ├── request/
│   │   │   │   │   │   └── CsvPersonDto.kt    # CSV person DTO
│   │   │   │   │   └── service/
│   │   │   │   │       └── CsvService.kt      # CSV processing business logic
│   │   │   │   │
│   │   │   │   └── todo/                      # Todo management feature
│   │   │   │       ├── controller/
│   │   │   │       │   └── TodoController.kt  # CRUD endpoints for todos
│   │   │   │       ├── dto/
│   │   │   │       │   ├── CreateTodoRequest.kt # Create todo request DTO
│   │   │   │       │   ├── TodoResponse.kt    # Todo response DTO
│   │   │   │       │   └── UpdateTodoRequest.kt # Update todo request DTO
│   │   │   │       ├── entity/
│   │   │   │       │   └── Todo.kt            # Todo entity
│   │   │   │       ├── repository/
│   │   │   │       │   ├── InMemoryTodoRepository.kt # In-memory implementation
│   │   │   │       │   └── TodoRepository.kt  # Repository interface
│   │   │   │       └── service/
│   │   │   │           └── TodoService.kt     # Todo business logic
│   │   │   │
│   │   │   ├── shared/                        # Shared utilities and cross-cutting concerns
│   │   │   │   ├── annotations/
│   │   │   │   │   └── AdminOnlyAnnotation.kt # Custom annotation for admin-only endpoints
│   │   │   │   └── response/
│   │   │   │       └── ApiResponse.kt         # Standardized API response wrapper
│   │   │   │
│   │   │   ├── GlobalExceptionHandler.kt      # Global exception handling with @ControllerAdvice
│   │   │   └── Application.kt                 # Spring Boot main application class
│   │   │
│   │   └── resources/
│   │       └── application.yaml               # Application configuration (server, database, JWT)
│   │
│   └── test/
│       └── kotlin/com/world/spring/
│           ├── todo/                          # Todo feature tests
│           │   ├── InMemoryTodoRepositoryTest.kt # Repository unit tests
│           │   ├── TodoControllerTest.kt      # Controller integration tests
│           │   └── TodoServiceTest.kt         # Service unit tests
│           └── ApplicationTests.kt            # Application context tests
│
├── gradle/                                    # Gradle wrapper files
├── build.gradle.kts                           # Gradle build configuration
├── settings.gradle.kts                        # Gradle settings
├── gradlew                                    # Gradle wrapper script (Unix)
├── gradlew.bat                                # Gradle wrapper script (Windows)
├── README.md                                  # This file
├── API_DOCUMENTATION.md                       # Detailed API documentation
├── Todo_API_Collection.json                   # Postman collection for API testing
└── Todo_API_Environment.json                  # Postman environment variables
```

## API Response Format

All API endpoints return responses in the following format:
```json
{
  "status": true/false,
  "message": "Success or error message",
  "data": {} or [] or null
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

## Validation Rules

- **Title**: Required, cannot be blank, max 255 characters
- **Description**: Optional, max 1000 characters
- **ID**: Must be a positive number (> 0)

## API Endpoints

See `API_DOCUMENTATION.md` for detailed API documentation.

## Swagger/OpenAPI Documentation

Interactive API documentation is available via Swagger UI:

- **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)
- **OpenAPI YAML**: [http://localhost:8081/v3/api-docs.yaml](http://localhost:8081/v3/api-docs.yaml)

### Using Swagger UI with Authentication

1. Navigate to the Swagger UI at `http://localhost:8081/swagger-ui.html`
2. Use the `/api/auth/login` endpoint to authenticate:
   - Default admin credentials: `username: admin`, `password: password`
   - Or register a new user via `/api/auth/register`
3. Copy the JWT token from the login response
4. Click the **"Authorize"** button at the top of the Swagger UI
5. Enter: `Bearer <your-token-here>` (include the word "Bearer" followed by a space)
6. Click **"Authorize"** and then **"Close"**
7. Now you can test all protected endpoints directly from Swagger UI

> **Note**: Swagger UI is accessible without authentication, but you need to authenticate to test protected endpoints.


## Running the Application

1. Make sure you have Java 21+ and Gradle installed
2. Run the application:
   ```bash
   ./gradlew bootRun
   ```
3. The application will start on `http://localhost:8081`

## Testing

Run the unit and integration tests:
```bash
./gradlew test
```

## Dependencies

- Spring Boot Web Starter
- Spring Security
- Spring Batch
- JJWT for JWT support
- Kotlin
- Jackson for JSON processing
- Mockito-Kotlin for testing

## Sample Data

The application includes some sample todos on startup that will be accessible through the API.