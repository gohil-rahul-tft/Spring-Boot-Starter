package com.world.spring.features.auth.controller

import com.world.spring.features.auth.request.AuthRequest
import com.world.spring.features.auth.response.AuthResponse
import com.world.spring.features.auth.request.RegisterRequest
import com.world.spring.shared.response.ApiResponse
import com.world.spring.features.auth.service.UserService
import com.world.spring.core.security.jwt.JwtTokenProvider
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
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<Unit>> {
        userService.register(request.username.trim(), request.password)
        return ResponseEntity.ok(ApiResponse.success(message = "User registered successfully"))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: AuthRequest): ResponseEntity<ApiResponse<AuthResponse>> {
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