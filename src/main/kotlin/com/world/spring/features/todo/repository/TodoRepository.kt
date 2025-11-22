package com.world.spring.features.todo.repository

import com.world.spring.features.todo.entity.Todo

interface TodoRepository {
    fun findAll(): List<Todo>
    fun findById(id: Long): Todo?
    fun save(todo: Todo): Todo
    fun update(id: Long, todo: Todo): Todo?
    fun deleteById(id: Long): Boolean
    fun deleteAll()
}