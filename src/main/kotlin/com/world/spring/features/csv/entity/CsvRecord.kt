package com.world.spring.features.csv.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Entity representing a CSV record stored in the database
 */
@Entity
@Table(name = "csv_records")
data class CsvRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "email", nullable = false)
    var email: String = "",

    @Column(name = "age", nullable = false)
    var age: Int = 0,

    @Column(name = "city", nullable = false)
    var city: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // No-argument constructor required for JPA/Hibernate
    private constructor() : this(null, "", "", 0, "", LocalDateTime.now(), LocalDateTime.now())
}