package com.world.spring.features.auth.repository

import com.world.spring.features.auth.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByUsername(username: String): Optional<UserEntity>
    fun existsByUsername(username: String): Boolean
}