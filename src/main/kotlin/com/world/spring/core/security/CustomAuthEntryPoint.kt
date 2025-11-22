package com.world.spring.core.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.world.spring.shared.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"

        val body = ApiResponse.error(
            message = "authentication failed",
            data = null
        )

        response.writer.write(objectMapper.writeValueAsString(body))
    }
}