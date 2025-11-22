package com.world.spring.features.todo.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// DTO for creating a new todo
data class CreateTodoRequest(
    @field:NotBlank(message = "Title cannot be blank")
    @field:Size(max = 255, message = "Title cannot exceed 255 characters")
    val title: String,

    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,

    val completed: Boolean = false
)
