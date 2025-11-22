package com.world.spring.features.todo.dto

import com.world.spring.features.todo.entity.Todo
import java.time.LocalDateTime

// Response DTO to return data to clients
data class TodoResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val completed: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// Extension function to convert Todo entity to TodoResponse DTO
fun Todo.toResponse(): TodoResponse {
    return TodoResponse(
        id = this.id!!,
        title = this.title,
        description = this.description,
        completed = this.completed,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
