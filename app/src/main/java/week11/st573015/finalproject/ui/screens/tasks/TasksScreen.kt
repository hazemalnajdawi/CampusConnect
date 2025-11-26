package week11.st573015.finalproject.ui.screens.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st573015.finalproject.models.Task
import week11.st573015.finalproject.vm.TasksViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    vm: TasksViewModel = viewModel()
) {
    val tasks by vm.tasks.collectAsState()
    val uiState by vm.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Task?>(null) }

        Box(modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (uiState is week11.st573015.finalproject.vm.UiState.Loading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tasks yet. Tap + to add one.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tasks, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                onEdit = {
                                    editing = it
                                    showDialog = true
                                },
                                onDelete = { vm.deleteTask(it) },
                                onToggleDone = {
                                    vm.updateTask(it.copy(isDone = !it.isDone))
                                }
                            )
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
            if (uiState is week11.st573015.finalproject.vm.UiState.Error) {
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter)) {
                    Text((uiState as week11.st573015.finalproject.vm.UiState.Error).message)
                    Button(onClick = { vm.clearError() }) { Text("Dismiss") }
                }
            }

        }

    if (showDialog) {
        AddEditTaskDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = { title, desc, due, reminder ->
                if (editing == null) vm.createTask(title, desc, due, reminder)
                else vm.updateTask(editing!!.copy(title = title, description = desc, dueTimestamp = due, reminderTimestamp = reminder))
                showDialog = false
            }
        )
    }
}

@Composable
private fun TaskRow(task: Task, onEdit: (Task) -> Unit, onDelete: (Task) -> Unit, onToggleDone: (Task) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isDone, onCheckedChange = { onToggleDone(task) })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                task.dueTimestamp?.let {
                    val fmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                    Text("Due: ${fmt.format(Date(it))}", style = MaterialTheme.typography.bodySmall)
                }
                if (task.description.isNotBlank()) {
                    Text(task.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
            }
            IconButton(onClick = { onEdit(task) }) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = { onDelete(task) }) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}