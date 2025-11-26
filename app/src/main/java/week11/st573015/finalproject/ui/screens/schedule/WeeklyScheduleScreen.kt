package week11.st573015.finalproject.ui.schedule

import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st573015.finalproject.models.ScheduleItem
import week11.st573015.finalproject.vm.ScheduleViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyScheduleScreen(
    navToMap: (() -> Unit)? = null,
    vm: ScheduleViewModel = viewModel()
) {
    val items by vm.items.collectAsState()
    val uiState by vm.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editing: ScheduleItem? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState is week11.st573015.finalproject.vm.ScheduleUiState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            WeeklyList(items = items, onEditRequest = {
                editing = it
                showAddDialog = true
            }, onDeleteRequest = { vm.deleteItem(it.id) })
        }
        FloatingActionButton(
            onClick = {
                editing = null
                showAddDialog = true
            }, modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add class")
        }
        if (uiState is week11.st573015.finalproject.vm.ScheduleUiState.Error) {
            Snackbar(modifier = Modifier.align(Alignment.BottomCenter)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text((uiState as week11.st573015.finalproject.vm.ScheduleUiState.Error).message)
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { vm.clearError() }) { Text("OK") }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditScheduleDialog(
            initial = editing,
            onDismiss = { showAddDialog = false },
            onSave = { item ->
                if (editing == null) vm.addItem(item) else vm.updateItem(item)
                showAddDialog = false
            },
            onDelete = { item ->
                vm.deleteItem(item.id)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun WeeklyList(
    items: List<ScheduleItem>,
    onEditRequest: (ScheduleItem) -> Unit,
    onDeleteRequest: (ScheduleItem) -> Unit
) {
    // Group by day 1..7
    val days = mapOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat",
        7 to "Sun"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(
            bottom = 90.dp // enough for FAB + elevation
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(days.toList()) { (dayNum, label) ->
            val dayItems = items.filter { it.dayOfWeek == dayNum }.sortedBy { it.startMinutes }
            DayCard(
                dayLabel = label,
                dayNum = dayNum,
                items = dayItems,
                onEdit = onEditRequest,
                onDelete = onDeleteRequest
            )
        }
    }
}

@Composable
private fun DayCard(
    dayLabel: String,
    dayNum: Int,
    items: List<ScheduleItem>,
    onEdit: (ScheduleItem) -> Unit,
    onDelete: (ScheduleItem) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    dayLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text("${items.size} classes", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No classes", color = Color.Gray)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEach { item ->
                        ScheduleRow(item = item, onEdit = onEdit, onDelete = onDelete)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleRow(
    item: ScheduleItem,
    onEdit: (ScheduleItem) -> Unit,
    onDelete: (ScheduleItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${formatMinutes(item.startMinutes)} - ${formatMinutes(item.endMinutes)} • ${item.room}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        IconButton(onClick = { onEdit(item) }) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit item",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = { onDelete(item) }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete item",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val hh = minutes / 60
    val mm = minutes % 60
    return String.format(Locale.getDefault(), "%02d:%02d", hh, mm)
}