package com.example.lab08

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TaskViewModel(private val dao: TaskDao) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    private val mutex = Mutex()

    init { change { } }

    // Serializar cada cambio y su lectura evita recargas fuera de orden.
    private fun change(operation: suspend () -> Unit) {
        viewModelScope.launch {
            mutex.withLock {
                operation()
                _tasks.value = dao.getAllTasks()
            }
        }
    }

    fun addTask(description: String) {
        val text = description.trim()
        if (text.isNotEmpty()) change { dao.insertTask(Task(description = text)) }
    }

    fun toggleTaskCompletion(task: Task) = change {
        val current = dao.getAllTasks().find { it.id == task.id }
        if (current != null) dao.updateTask(current.copy(isCompleted = !current.isCompleted))
    }

    fun deleteAllTasks() = change { dao.deleteAllTasks() }

    class Factory(private val dao: TaskDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TaskViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(dao) as T
        }
    }
}
