package com.world.spring.features.auth.service

import com.world.spring.features.auth.enums.RoleEnum
import com.world.spring.features.auth.entity.UserEntity
import com.world.spring.features.auth.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * UserService:
 * - register(username, rawPassword): creates a USER by default
 * - registerAdmin(...) helper (used by DataInitializer) to create ADMIN
 */


@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun register(username: String, password: String, roleEnum: RoleEnum = RoleEnum.USER): UserEntity {

        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("username already taken")
        }
        val encoded = passwordEncoder.encode(password)
        val userEntity = UserEntity(username = username, password = encoded, roleEnum = roleEnum)
        return userRepository.save(userEntity)
    }

    fun findByUsername(username: String): UserEntity? =
        userRepository.findByUsername(username).orElse(null)
}