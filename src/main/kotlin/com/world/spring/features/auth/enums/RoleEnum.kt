package com.world.spring.features.auth.enums

/**
 * Role enum for simple RBAC.
 *
 * We keep two roles for now:
 * - USER: regular user (can create/read/update todos but cannot delete others)
 * - ADMIN: full access (including delete)
 */
enum class RoleEnum {
    USER,
    ADMIN
}