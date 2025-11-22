package com.world.spring.shared.annotations

import org.springframework.security.access.prepost.PreAuthorize


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole(T(com.world.spring.features.auth.enums.RoleEnum).ADMIN.name())")
annotation class AdminOnly

