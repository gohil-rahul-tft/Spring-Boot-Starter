package com.world.spring.features.todo.repository

import com.world.spring.features.todo.entity.Todo
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Repository
class InMemoryTodoRepository : TodoRepository {
    private val todos = ConcurrentHashMap<Long, Todo>()
    private val idGenerator = AtomicLong(1)

    init {
        // Add some sample data
        save(Todo(title = "Learn Spring Boot", description = "Create a todo application with CRUD operations", completed = false))
        save(Todo(title = "Build API", description = "Create REST endpoints for todo management", completed = true))
        save(Todo(title = "Test Application", description = "Write unit tests for the application", completed = false))
    }

    override fun findAll(): List<Todo> {
        return todos.values.toList()
    }

    override fun findAllByUserId(userId: Long): List<Todo> {
        return todos.values.filter { it.userId == userId }
    }

    override fun findById(id: Long): Todo? {
        return todos[id]
    }

    override fun save(todo: Todo): Todo {
        val id = todo.id ?: idGenerator.getAndIncrement()
        val newTodo = todo.copy(
            id = id,
            createdAt = if (todo.id == null) LocalDateTime.now() else todo.createdAt,
            updatedAt = LocalDateTime.now()
        )
        todos[id] = newTodo
        return newTodo
    }

    override fun update(id: Long, todo: Todo): Todo? {
        val existingTodo = todos[id] ?: return null
        val updatedTodo = existingTodo.copy(
            title = todo.title,
            description = todo.description,
            completed = todo.completed,
            updatedAt = LocalDateTime.now()
        )
        todos[id] = updatedTodo
        return updatedTodo
    }

    override fun deleteById(id: Long): Boolean {
        return todos.remove(id) != null
    }

    override fun deleteAll() {
        todos.clear()
    }
}