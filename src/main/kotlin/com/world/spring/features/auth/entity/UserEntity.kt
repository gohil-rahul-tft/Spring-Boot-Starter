package com.world.spring.features.auth.entity

import com.world.spring.features.auth.enums.RoleEnum
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * Simple User entity. Password stored as encoded value (BCrypt).
 * Fields: id, username (unique), password (encoded), createdAt
 */
@Entity
@Table(name = "app_users", uniqueConstraints = [UniqueConstraint(columnNames = ["username"])])
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    val username: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val roleEnum: RoleEnum = RoleEnum.USER  // default role USER
)