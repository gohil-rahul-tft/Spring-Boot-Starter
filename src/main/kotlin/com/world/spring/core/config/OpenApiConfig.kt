package com.world.spring.core.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI/Swagger configuration for API documentation.
 * Swagger UI will be available at: /swagger-ui.html
 * OpenAPI JSON will be available at: /v3/api-docs
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"
        
        return OpenAPI()
            .info(
                Info()
                    .title("Spring Boot Todo API")
                    .description("""
                        RESTful API for managing todos with JWT authentication.
                        
                        ## Features
                        - Full CRUD operations for todos
                        - JWT-based authentication and authorization
                        - User management with role-based access control
                        - CSV file processing (single and batch)
                        - Standardized API response format
                        
                        ## Authentication
                        1. Register a new user via `/api/auth/register` or login with existing credentials via `/api/auth/login`
                        2. Copy the JWT token from the response
                        3. Click the "Authorize" button above
                        4. Enter: `Bearer <your-token-here>`
                        5. Click "Authorize" and then "Close"
                        6. Now you can access protected endpoints
                        
                        ## Default Admin User
                        - Username: `admin`
                        - Password: `password`
                    """.trimIndent())
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("API Support")
                            .email("support@example.com")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Enter JWT token obtained from /api/auth/login endpoint")
                    )
            )
    }
}
