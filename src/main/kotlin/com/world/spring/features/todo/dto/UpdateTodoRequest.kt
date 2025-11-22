package com.world.spring.features.todo.dto

import jakarta.validation.constraints.Size

// DTO for updating an existing todo
data class UpdateTodoRequest(
    @field:Size(max = 255, message = "Title cannot exceed 255 characters")
    val title: String? = null,

    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,

    val completed: Boolean? = null
)
