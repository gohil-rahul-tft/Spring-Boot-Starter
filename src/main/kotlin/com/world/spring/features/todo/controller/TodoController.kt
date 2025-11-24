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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todos")
@Tag(name = "Todo Management", description = "CRUD operations for managing todos")
@SecurityRequirement(name = "bearerAuth")
class TodoController(private val todoService: TodoService) {

    @GetMapping
    @Operation(
        summary = "Get all todos",
        description = "Retrieves a list of all todos for the authenticated user"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Todos retrieved successfully"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
        ]
    )
    fun getAllTodos(): ResponseEntity<ApiResponse<List<TodoResponse>>> {
        val todos = todoService.getAllTodos().map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse.success(todos, "Todos retrieved successfully"))
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get todo by ID",
        description = "Retrieves a specific todo by its ID"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Todo retrieved successfully"),
            SwaggerApiResponse(responseCode = "400", description = "Bad request - Invalid ID format"),
            SwaggerApiResponse(responseCode = "404", description = "Todo not found"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getTodoById(
        @Parameter(description = "ID of the todo to retrieve", required = true)
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<TodoResponse>> {
        validateId(id)
        val todo = todoService.getTodoById(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error<TodoResponse>("Todo with ID $id not found"))
        return ResponseEntity.ok(ApiResponse.success(todo.toResponse(), "Todo retrieved successfully"))
    }

    @PostMapping
    @Operation(
        summary = "Create a new todo",
        description = "Creates a new todo item with the provided details"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "201", description = "Todo created successfully"),
            SwaggerApiResponse(responseCode = "400", description = "Bad request - Invalid input"),
            SwaggerApiResponse(responseCode = "422", description = "Validation error"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun createTodo(
        @Parameter(description = "Todo creation request", required = true)
        @Valid @RequestBody createRequest: CreateTodoRequest
    ): ResponseEntity<ApiResponse<TodoResponse>> {
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
    @Operation(
        summary = "Update todo (full update)",
        description = "Updates an existing todo with all provided fields. Null fields will be set to default values."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Todo updated successfully"),
            SwaggerApiResponse(responseCode = "400", description = "Bad request - Invalid ID or input"),
            SwaggerApiResponse(responseCode = "404", description = "Todo not found"),
            SwaggerApiResponse(responseCode = "422", description = "Validation error"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun updateTodo(
        @Parameter(description = "ID of the todo to update", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Todo update request", required = true)
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
    @Operation(
        summary = "Patch todo (partial update)",
        description = "Partially updates an existing todo. Only provided fields will be updated, null fields will be ignored."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Todo updated successfully"),
            SwaggerApiResponse(responseCode = "400", description = "Bad request - Invalid ID or input"),
            SwaggerApiResponse(responseCode = "404", description = "Todo not found"),
            SwaggerApiResponse(responseCode = "422", description = "Validation error"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun patchTodo(
        @Parameter(description = "ID of the todo to patch", required = true)
        @PathVariable id: Long,
        @Parameter(description = "Todo patch request with optional fields", required = true)
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
    @Operation(
        summary = "Delete todo by ID (Admin only)",
        description = "Deletes a specific todo by its ID. Requires ADMIN role."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Todo deleted successfully"),
            SwaggerApiResponse(responseCode = "400", description = "Bad request - Invalid ID"),
            SwaggerApiResponse(responseCode = "404", description = "Todo not found"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            SwaggerApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
        ]
    )
    fun deleteTodo(
        @Parameter(description = "ID of the todo to delete", required = true)
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
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
    @Operation(
        summary = "Delete all todos (Admin only)",
        description = "Deletes all todos. Requires ADMIN role. Use with caution!"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "All todos deleted successfully"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            SwaggerApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
        ]
    )
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