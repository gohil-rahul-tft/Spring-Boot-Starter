package com.world.spring.core.security.jwt

import com.world.spring.core.security.jwt.JwtTokenProvider
import com.world.spring.core.security.service.LocalUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter that:
 * - extracts Bearer token from Authorization header
 * - validates token
 * - loads user details and sets Authentication in SecurityContext
 */

@Component
class JwtAuthFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val localUserDetailsService: LocalUserDetailsService
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = resolveToken(request)
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                val username = jwtTokenProvider.getUsernameFromToken(jwt)
                val userDetails = localUserDetailsService.loadUserByUsername(username)

                val auth = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = auth
            }
        } catch (ex: Exception) {
            // In case of failure, clear context and proceed (request will be rejected by AccessDecision).
            SecurityContextHolder.clearContext()
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization")
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7)
        }
        return null
    }

}