package com.world.spring.shared

import com.world.spring.features.auth.enums.RoleEnum
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

/**
 * Utility object for extracting user information from Spring Security context
 */
object SecurityUtils {

    /**
     * Get the currently authenticated username
     * @return username or null if not authenticated
     */
    fun getCurrentUsername(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        return when (val principal = authentication?.principal) {
            is UserDetails -> principal.username
            is String -> principal
            else -> null
        }
    }

    /**
     * Check if the current user has ADMIN role
     * @return true if user is admin, false otherwise
     */
    fun isCurrentUserAdmin(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.authorities?.any { 
            it.authority == "ROLE_${RoleEnum.ADMIN.name}" 
        } ?: false
    }

    /**
     * Get the current user's role
     * @return RoleEnum or null if not authenticated
     */
    fun getCurrentUserRole(): RoleEnum? {
        val authentication = SecurityContextHolder.getContext().authentication
        val adminAuthority = authentication?.authorities?.firstOrNull { 
            it.authority.startsWith("ROLE_") 
        }
        
        return when {
            adminAuthority?.authority == "ROLE_${RoleEnum.ADMIN.name}" -> RoleEnum.ADMIN
            adminAuthority?.authority == "ROLE_${RoleEnum.USER.name}" -> RoleEnum.USER
            else -> null
        }
    }
}
