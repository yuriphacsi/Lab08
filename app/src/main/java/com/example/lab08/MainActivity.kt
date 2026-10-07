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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.shape.RoundedCornerShape
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
    var editingId by rememberSaveable { mutableStateOf<Int?>(null) }
    var editedDescription by rememberSaveable { mutableStateOf("") }
    val editingTask = tasks.find { it.id == editingId }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(Modifier.padding(padding).imePadding().fillMaxSize().padding(16.dp)) {
            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Mis tareas", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    Text("Lab08 · Organiza tus pendientes", color = Color.White)
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = description, onValueChange = { description = it },
                label = { Text("Nueva tarea") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                viewModel.addTask(description)
                description = ""
            }, enabled = description.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text("Agregar tarea")
            }
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)) {
                if (tasks.isEmpty()) item {
                    Text("No tienes tareas. Agrega la primera.", modifier = Modifier.padding(vertical = 16.dp))
                }
                items(tasks, key = { it.id }) { task ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.fillMaxWidth().padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTaskCompletion(task) })
                                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(task.description,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None)
                                    Text(if (task.isCompleted) "Completada" else "Pendiente",
                                        style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = {
                                    editingId = task.id
                                    editedDescription = task.description
                                }) { Text("Editar") }
                                TextButton(onClick = { viewModel.deleteTask(task) }) {
                                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
            OutlinedButton(onClick = viewModel::deleteAllTasks, enabled = tasks.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()) { Text("Eliminar todas las tareas") }
        }
    }
    if (editingTask != null) {
        AlertDialog(onDismissRequest = { editingId = null },
            title = { Text("Editar tarea") },
            text = {
                OutlinedTextField(value = editedDescription, onValueChange = { editedDescription = it },
                    label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.editTask(editingTask, editedDescription)
                    editingId = null
                }, enabled = editedDescription.isNotBlank()) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { editingId = null }) { Text("Cancelar") } })
    }
}
