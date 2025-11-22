package com.world.spring.features.auth.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * RegisterRequest DTO — used for /api/auth/register
 */

data class RegisterRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 100)
    val username: String = "",

    @field:NotBlank
    @field:Size(min = 6, max = 128)
    val password: String = ""
)