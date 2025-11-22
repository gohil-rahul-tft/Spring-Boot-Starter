package com.world.spring.features.todo.service

import com.world.spring.features.todo.entity.Todo
import com.world.spring.features.todo.repository.TodoRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class TodoService(private val todoRepository: TodoRepository) {

    fun getAllTodos(): List<Todo> {
        return todoRepository.findAll()
    }

    fun getTodoById(id: Long): Todo? {
        return todoRepository.findById(id)
    }

    fun createTodo(todo: Todo): Todo {
        return todoRepository.save(todo.copy(id = null)) // Ensure ID is null for new creation
    }

    fun updateTodo(id: Long, todo: Todo): Todo? {
        return todoRepository.update(id, todo)
    }

    fun deleteTodo(id: Long): Boolean {
        return todoRepository.deleteById(id)
    }

    fun deleteAllTodos(): Boolean {
        todoRepository.deleteAll()
        return true
    }
}