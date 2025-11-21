package com.world.spring.user

import jakarta.persistence.*
import java.time.Instant

/**
 * Simple User entity. Password stored as encoded value (BCrypt).
 * Fields: id, username (unique), password (encoded), createdAt
 */
@Entity
@Table(name = "app_users", uniqueConstraints = [UniqueConstraint(columnNames = ["username"])])
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    val username: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)
