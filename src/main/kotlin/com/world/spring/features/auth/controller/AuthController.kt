package com.world.spring.features.auth.controller

import com.world.spring.features.auth.request.AuthRequest
import com.world.spring.features.auth.response.AuthResponse
import com.world.spring.features.auth.request.RegisterRequest
import com.world.spring.shared.response.ApiResponse
import com.world.spring.features.auth.service.UserService
import com.world.spring.core.security.jwt.JwtTokenProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.core.AuthenticationException

/**
 * AuthController:
 * - POST /api/auth/register
 * - POST /api/auth/login
 *
 * Registration will create a user and return success wrapper.
 * Login will authenticate and return token as AuthResponse in wrapper.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and authentication endpoints")
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
) {

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with USER role. Username must be unique."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "User registered successfully"),
            SwaggerApiResponse(responseCode = "400", description = "Bad request - Username already exists"),
            SwaggerApiResponse(responseCode = "422", description = "Validation error - Invalid input")
        ]
    )
    fun register(
        @Parameter(description = "Registration request with username and password", required = true)
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.register(request.username.trim(), request.password)
        return ResponseEntity.ok(ApiResponse.success(message = "User registered successfully"))
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login and get JWT token",
        description = "Authenticates user credentials and returns a JWT token for accessing protected endpoints. Default admin credentials: username='admin', password='password'"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Login successful - JWT token returned"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized - Invalid username or password"),
            SwaggerApiResponse(responseCode = "422", description = "Validation error - Invalid input")
        ]
    )
    fun login(
        @Parameter(description = "Login request with username and password", required = true)
        @Valid @RequestBody request: AuthRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        return try {
            // Attempt authentication
            val authToken = UsernamePasswordAuthenticationToken(request.username, request.password)
            authenticationManager.authenticate(authToken)

            // If we reach here, authentication succeeded
            val token = jwtTokenProvider.generateToken(request.username)
            ResponseEntity.ok(ApiResponse.success(message = "Login successful", data = AuthResponse(token)))


        } catch (ex: AuthenticationException) {
            ResponseEntity.status(401).body(ApiResponse.error(message = "Invalid username/password"))
        }
    }
}