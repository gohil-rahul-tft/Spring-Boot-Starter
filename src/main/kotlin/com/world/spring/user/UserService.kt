package com.world.spring.user


import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * A thin service responsible for user creation and lookup.
 * - register: encodes password and persists user
 * - findByUsername: returns user or null
 */


@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun register(username: String, password: String): User {

        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("username already taken")
        }
        val encoded = passwordEncoder.encode(password)
        val user = User(username = username, password = encoded)
        return userRepository.save(user)
    }

    fun findByUsername(username: String): User? =
        userRepository.findByUsername(username).orElse(null)
}