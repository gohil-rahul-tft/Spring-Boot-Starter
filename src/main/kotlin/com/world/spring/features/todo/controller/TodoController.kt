package com.world.spring.features.todo.controller

import com.world.spring.shared.annotations.AdminOnly
import com.world.spring.shared.response.ApiResponse
import com.world.spring.features.todo.dto.CreateTodoRequest
import com.world.spring.features.todo.dto.UpdateTodoRequest
import com.world.spring.features.todo.dto.TodoResponse
import com.world.spring.features.todo.dto.toResponse
import com.world.spring.features.todo.entity.Todo
import com.world.spring.features.todo.entity.applyUpdate
import com.world.spring.features.todo.service.TodoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todos")
class TodoController(private val todoService: TodoService) {

    @GetMapping
    fun getAllTodos(): ResponseEntity<ApiResponse<List<TodoResponse>>> {
        val todos = todoService.getAllTodos().map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse.success(todos, "Todos retrieved successfully"))
    }

    @GetMapping("/{id}")
    fun getTodoById(@PathVariable id: Long): ResponseEntity<ApiResponse<TodoResponse>> {
        validateId(id)
        val todo = todoService.getTodoById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error<TodoResponse>("Todo with ID $id not found"))
        return ResponseEntity.ok(ApiResponse.success(todo.toResponse(), "Todo retrieved successfully"))
    }

    @PostMapping
    fun createTodo(@Valid @RequestBody createRequest: CreateTodoRequest): ResponseEntity<ApiResponse<TodoResponse>> {
        val newTodo = Todo(
            title = createRequest.title.trim(),
            description = createRequest.description,
            completed = createRequest.completed
        )
        val savedTodo = todoService.createTodo(newTodo)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(savedTodo.toResponse(), "Todo created successfully"))
    }

    @PutMapping("/{id}")
    fun updateTodo(
        @PathVariable id: Long,
        @Valid @RequestBody updateRequest: UpdateTodoRequest,
    ): ResponseEntity<ApiResponse<TodoResponse>> {
        validateId(id)

        val existingTodo = todoService.getTodoById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Todo with ID $id not found"))

        val todoToUpdate = existingTodo.copy(
            title = updateRequest.title?.trim() ?: existingTodo.title,
            description = updateRequest.description,
            completed = updateRequest.completed ?: existingTodo.completed
        )

        val updatedTodo = todoService.updateTodo(id, todoToUpdate)
            ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update todo"))

        return ResponseEntity.ok(ApiResponse.success(updatedTodo.toResponse(), "Todo updated successfully"))
    }

    @PatchMapping("/{id}")
    fun patchTodo(
        @PathVariable id: Long,
        @Valid @RequestBody updateRequest: UpdateTodoRequest,
    ): ResponseEntity<ApiResponse<TodoResponse>> {
        validateId(id)

        val existingTodo = todoService.getTodoById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Todo with ID $id not found"))

        val todoToPatch = existingTodo.applyUpdate(updateRequest)
        val patchedTodo = todoService.updateTodo(id, todoToPatch)
            ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update todo"))

        return ResponseEntity.ok(ApiResponse.success(patchedTodo.toResponse(), "Todo updated successfully"))
    }

    /**
     * Only ADMIN can delete todos.
     * USER role will be rejected with 403 (access denied) handled by GlobalExceptionHandler.
     */
    @AdminOnly
    @DeleteMapping("/{id}")
    fun deleteTodo(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        validateId(id)
        val deleted = todoService.deleteTodo(id)
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Todo with ID $id not found"))
        }
        return ResponseEntity.ok(ApiResponse.success("Todo deleted successfully"))
    }

    /**
     * Only ADMIN can delete todos.
     * USER role will be rejected with 403 (access denied) handled by GlobalExceptionHandler.
     */
    @AdminOnly
    @DeleteMapping
    fun deleteAllTodos(): ResponseEntity<ApiResponse<Unit>> {
        todoService.deleteAllTodos()
        return ResponseEntity.ok(ApiResponse.success("All todos deleted successfully"))
    }

    private fun validateId(id: Long) {
        if (id <= 0) {
            throw IllegalArgumentException("ID must be a positive number")
        }
    }
}