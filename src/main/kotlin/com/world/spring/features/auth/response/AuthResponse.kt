package com.world.spring.features.auth.response

/**
 * Response returned after successful authentication.
 * Contains the JWT token and token type (Bearer).
 */
data class AuthResponse(
    val token: String,
    val tokenType: String = "Bearer"
)