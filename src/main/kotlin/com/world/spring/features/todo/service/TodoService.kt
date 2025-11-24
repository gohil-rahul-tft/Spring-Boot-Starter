package com.world.spring.features.todo.service

import com.world.spring.features.todo.entity.Todo
import com.world.spring.features.todo.repository.TodoRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service

@Service
class TodoService(private val todoRepository: TodoRepository) {

    /**
     * Get all todos for a user (or all todos if admin)
     * @param userId - ID of the authenticated user
     * @param isAdmin - true if user has ADMIN role
     * @return list of todos
     */
    fun getAllTodos(userId: Long, isAdmin: Boolean): List<Todo> {
        return if (isAdmin) {
            todoRepository.findAll()
        } else {
            todoRepository.findAllByUserId(userId)
        }
    }

    /**
     * Get todo by ID with ownership validation
     * @param id - todo ID
     * @param userId - ID of the authenticated user
     * @param isAdmin - true if user has ADMIN role
     * @return todo if found and user has access
     * @throws AccessDeniedException if user doesn't own the todo
     */
    fun getTodoById(id: Long, userId: Long, isAdmin: Boolean): Todo? {
        val todo = todoRepository.findById(id) ?: return null
        
        // Check ownership unless user is admin
        if (!isAdmin && todo.userId != userId) {
            throw AccessDeniedException("You don't have permission to access this todo")
        }
        
        return todo
    }

    /**
     * Create a new todo with the authenticated user's ID
     * @param todo - todo to create
     * @param userId - ID of the authenticated user
     * @return created todo
     */
    fun createTodo(todo: Todo, userId: Long): Todo {
        val todoWithUser = todo.copy(id = null, userId = userId)
        return todoRepository.save(todoWithUser)
    }

    /**
     * Update a todo with ownership validation
     * @param id - todo ID
     * @param todo - updated todo data
     * @param userId - ID of the authenticated user
     * @param isAdmin - true if user has ADMIN role
     * @return updated todo
     * @throws AccessDeniedException if user doesn't own the todo
     */
    fun updateTodo(id: Long, todo: Todo, userId: Long, isAdmin: Boolean): Todo? {
        val existingTodo = todoRepository.findById(id) ?: return null
        
        // Check ownership unless user is admin
        if (!isAdmin && existingTodo.userId != userId) {
            throw AccessDeniedException("You don't have permission to update this todo")
        }
        
        // Preserve the original userId
        val todoToUpdate = todo.copy(userId = existingTodo.userId)
        return todoRepository.update(id, todoToUpdate)
    }

    /**
     * Delete a todo with ownership validation
     * @param id - todo ID
     * @param userId - ID of the authenticated user
     * @param isAdmin - true if user has ADMIN role
     * @return true if deleted
     * @throws AccessDeniedException if user doesn't own the todo
     */
    fun deleteTodo(id: Long, userId: Long, isAdmin: Boolean): Boolean {
        val existingTodo = todoRepository.findById(id) ?: return false
        
        // Check ownership unless user is admin
        if (!isAdmin && existingTodo.userId != userId) {
            throw AccessDeniedException("You don't have permission to delete this todo")
        }
        
        return todoRepository.deleteById(id)
    }

    /**
     * Delete all todos (admin only - enforced by @AdminOnly annotation)
     */
    fun deleteAllTodos(): Boolean {
        todoRepository.deleteAll()
        return true
    }
}