package com.world.spring.auth.dto

import jakarta.validation.constraints.NotBlank

/**
 * Login request DTO — /api/auth/login
 */
data class AuthRequest(
    @field:NotBlank
    val username: String = "",

    @field:NotBlank
    val password: String = ""
)