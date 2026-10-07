package com.example.lab08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lab08.ui.theme.Lab08Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dao = TaskDatabase.getInstance(applicationContext).taskDao()
        setContent {
            Lab08Theme {
                val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory(dao))
                TaskScreen(taskViewModel)
            }
        }
    }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var description by rememberSaveable { mutableStateOf("") }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.safeDrawingPadding().imePadding().padding(16.dp)) {
            TextField(value = description, onValueChange = { description = it },
                label = { Text("Nueva tarea") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                viewModel.addTask(description)
                description = ""
            }, enabled = description.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text("Agregar tarea")
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(tasks, key = { it.id }) { task ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(task.description, modifier = Modifier.weight(1f))
                        Button(onClick = { viewModel.toggleTaskCompletion(task) }) {
                            Text(if (task.isCompleted) "Completada" else "Pendiente")
                        }
                    }
                }
            }
            Button(onClick = viewModel::deleteAllTasks, modifier = Modifier.fillMaxWidth()) {
                Text("Eliminar todas las tareas")
            }
        }
    }
}
