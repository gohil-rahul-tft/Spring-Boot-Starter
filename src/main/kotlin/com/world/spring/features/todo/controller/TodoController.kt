package com.world.spring.features.todo.controller

import com.world.spring.features.auth.service.UserService
import com.world.spring.shared.SecurityUtils
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
@Tag(name = "Todo Management", description = "CRUD operations for managing todos with user-based authorization")
@SecurityRequirement(name = "bearerAuth")
class TodoController(
    private val todoService: TodoService,
    private val userService: UserService
) {

    @GetMapping
    @Operation(
        summary = "Get all todos",
        description = "Retrieves todos for the authenticated user. Admins can see all todos, regular users only see their own."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Todos retrieved successfully"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
        ]
    )
    fun getAllTodos(): ResponseEntity<ApiResponse<List<TodoResponse>>> {
        val (userId, isAdmin) = getUserContext()
        val todos = todoService.getAllTodos(userId, isAdmin).map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse.success(todos, "Todos retrieved successfully"))
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get todo by ID",
        description = "Retrieves a specific todo by its ID. Users can only view their own todos, admins can view any todo."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Todo retrieved successfully"),
            SwaggerApiResponse(responseCode = "400", description = "Bad request - Invalid ID format"),
            SwaggerApiResponse(responseCode = "403", description = "Forbidden - You don't have permission to access this todo"),
            SwaggerApiResponse(responseCode = "404", description = "Todo not found"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getTodoById(
        @Parameter(description = "ID of the todo to retrieve", required = true)
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<TodoResponse>> {
        validateId(id)
        val (userId, isAdmin) = getUserContext()
        val todo = todoService.getTodoById(id, userId, isAdmin)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error<TodoResponse>("Todo with ID $id not found"))
        return ResponseEntity.ok(ApiResponse.success(todo.toResponse(), "Todo retrieved successfully"))
    }

    @PostMapping
    @Operation(
        summary = "Create a new todo",
        description = "Creates a new todo item for the authenticated user. The userId is automatically set from the authentication context."
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
        val (userId, _) = getUserContext()
        
        val newTodo = Todo(
            title = createRequest.title.trim(),
            description = createRequest.description,
            completed = createRequest.completed
        )
        val savedTodo = todoService.createTodo(newTodo, userId)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(savedTodo.toResponse(), "Todo created successfully"))
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update todo (full update)",
        description = "Updates an existing todo with all provided fields. Users can only update their own todos, admins can update any todo."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Todo updated successfully"),
            SwaggerApiResponse(responseCode = "400", description = "Bad request - Invalid ID or input"),
            SwaggerApiResponse(responseCode = "403", description = "Forbidden - You don't have permission to update this todo"),
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
        val (userId, isAdmin) = getUserContext()

        val existingTodo = todoService.getTodoById(id, userId, isAdmin)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Todo with ID $id not found"))

        val todoToUpdate = existingTodo.copy(
            title = updateRequest.title?.trim() ?: existingTodo.title,
            description = updateRequest.description,
            completed = updateRequest.completed ?: existingTodo.completed
        )

        val updatedTodo = todoService.updateTodo(id, todoToUpdate, userId, isAdmin)
            ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update todo"))

        return ResponseEntity.ok(ApiResponse.success(updatedTodo.toResponse(), "Todo updated successfully"))
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Patch todo (partial update)",
        description = "Partially updates an existing todo. Only provided fields will be updated. Users can only update their own todos, admins can update any todo."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Todo updated successfully"),
            SwaggerApiResponse(responseCode = "400", description = "Bad request - Invalid ID or input"),
            SwaggerApiResponse(responseCode = "403", description = "Forbidden - You don't have permission to update this todo"),
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
        val (userId, isAdmin) = getUserContext()

        val existingTodo = todoService.getTodoById(id, userId, isAdmin)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Todo with ID $id not found"))

        val todoToPatch = existingTodo.applyUpdate(updateRequest)
        val patchedTodo = todoService.updateTodo(id, todoToPatch, userId, isAdmin)
            ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update todo"))

        return ResponseEntity.ok(ApiResponse.success(patchedTodo.toResponse(), "Todo updated successfully"))
    }

    /**
     * Delete a todo by ID.
     * Users can delete their own todos, admins can delete any todo.
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete todo by ID",
        description = "Deletes a specific todo by its ID. Users can only delete their own todos, admins can delete any todo."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Todo deleted successfully"),
            SwaggerApiResponse(responseCode = "400", description = "Bad request - Invalid ID"),
            SwaggerApiResponse(responseCode = "403", description = "Forbidden - You don't have permission to delete this todo"),
            SwaggerApiResponse(responseCode = "404", description = "Todo not found"),
            SwaggerApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun deleteTodo(
        @Parameter(description = "ID of the todo to delete", required = true)
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        validateId(id)
        val (userId, isAdmin) = getUserContext()
        
        val deleted = todoService.deleteTodo(id, userId, isAdmin)
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Todo with ID $id not found"))
        }
        return ResponseEntity.ok(ApiResponse.success("Todo deleted successfully"))
    }

    /**
     * Only ADMIN can delete all todos.
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

    /**
     * Helper method to validate ID
     */
    private fun validateId(id: Long) {
        if (id <= 0) {
            throw IllegalArgumentException("ID must be a positive number")
        }
    }

    /**
     * Helper method to get current user context
     * @return Pair of (userId, isAdmin)
     */
    private fun getUserContext(): Pair<Long, Boolean> {
        val username = SecurityUtils.getCurrentUsername()
            ?: throw IllegalStateException("No authenticated user found")
        
        val userId = userService.getUserIdByUsername(username)
            ?: throw IllegalStateException("User not found: $username")
        
        val isAdmin = SecurityUtils.isCurrentUserAdmin()
        
        return Pair(userId, isAdmin)
    }
}