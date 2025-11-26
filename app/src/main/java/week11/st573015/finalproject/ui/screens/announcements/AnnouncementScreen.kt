package week11.st573015.finalproject.ui.screens.announcements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import week11.st573015.finalproject.models.Announcement
import week11.st573015.finalproject.vm.AnnouncementUiState
import week11.st573015.finalproject.vm.AnnouncementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementScreen(
    navController: NavController,
    vm: AnnouncementViewModel = viewModel()
) {
    val announcements by vm.announcements.collectAsState()
    val uiState by vm.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Announcements") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Announcement")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            when {
                uiState is AnnouncementUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                announcements.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No announcements yet.")
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(announcements, key = { it.id }) { announcement ->
                            AnnouncementRow(
                                announcement = announcement,
                                onDelete = { vm.deleteAnnouncement(it) }
                            )
                        }
                    }
                }
            }

            if (uiState is AnnouncementUiState.Error) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text((uiState as AnnouncementUiState.Error).message)
                    Button(onClick = { vm.clearError() }) { Text("Dismiss") }
                }
            }
        }
    }

    if (showDialog) {
        AddAnnouncementDialog(
            onDismiss = { showDialog = false },
            onSave = { title, message ->
                vm.addAnnouncement(title, message)
                showDialog = false
            }
        )
    }
}

@Composable
private fun AnnouncementRow(announcement: Announcement, onDelete: (Announcement) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(announcement.title, style = MaterialTheme.typography.titleMedium)
                Text(announcement.message, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = { onDelete(announcement) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Announcement")
            }
        }
    }
}

@Composable
private fun AddAnnouncementDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, message: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Announcement") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message") })
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank() && message.isNotBlank())
                    onSave(title.trim(), message.trim())
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
