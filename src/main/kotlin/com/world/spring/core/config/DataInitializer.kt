package com.world.spring.core.config

import com.world.spring.features.auth.enums.RoleEnum
import com.world.spring.features.auth.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * DataInitializer creates an 'admin' user on startup if it doesn't exist.
 * This is for development convenience only. Remove or protect in production.
 */
@Configuration
class DataInitializer {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun createAdminUser(userService: UserService): CommandLineRunner {
        return CommandLineRunner {
            val adminUsername = "admin"
            val adminPassword = "password" // change or use env var in real dev
            val existing = userService.findByUsername(adminUsername)
            if (existing == null) {
                userService.register(adminUsername, adminPassword, RoleEnum.ADMIN)
                log.info("Created default admin user '{}' (password: {})", adminUsername, adminPassword)
            } else {
                log.info("Admin user '{}' already exists", adminUsername)
            }
        }
    }
}
