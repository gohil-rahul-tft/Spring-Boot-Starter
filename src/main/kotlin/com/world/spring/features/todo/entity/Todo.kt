package com.world.spring.features.todo.entity

import com.world.spring.features.todo.dto.UpdateTodoRequest
import java.time.LocalDateTime

data class Todo(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    val completed: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

// Extension function to apply update fields to a Todo entity
fun Todo.applyUpdate(updateRequest: UpdateTodoRequest): Todo {
    return this.copy(
        title = updateRequest.title ?: this.title,
        description = updateRequest.description ?: this.description,
        completed = updateRequest.completed ?: this.completed
    )
}