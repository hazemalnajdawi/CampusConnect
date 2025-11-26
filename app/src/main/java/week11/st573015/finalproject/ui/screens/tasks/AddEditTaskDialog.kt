package week11.st573015.finalproject.ui.screens.tasks

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import week11.st573015.finalproject.models.Task
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddEditTaskDialog(
    initial: Task?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, dueTimestamp: Long?, reminderTimestamp: Long?) -> Unit
) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var dueTs by remember { mutableStateOf<Long?>(initial?.dueTimestamp) }
    var reminderTs by remember { mutableStateOf<Long?>(initial?.reminderTimestamp) }

    val df = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    var dueText by remember { mutableStateOf(if (dueTs != null) df.format(Date(dueTs!!)) else "Not set") }
    var reminderText by remember { mutableStateOf(if (reminderTs != null) df.format(Date(reminderTs!!)) else "Not set") }

    fun pickDateTime(onPicked: (Long) -> Unit) {
        val now = Calendar.getInstance()
        val dp = android.app.DatePickerDialog(ctx, { _, y, m, d ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
            }
            val hour = now.get(Calendar.HOUR_OF_DAY)
            val minute = now.get(Calendar.MINUTE)
            TimePickerDialog(ctx, { _, h, min ->
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                onPicked(cal.timeInMillis)
            }, hour, minute, true).show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
        dp.show()
    }

    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (initial == null) "Add Task" else "Edit Task") }, text = {
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Due: $dueText")
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(onClick = {
                        pickDateTime {
                            dueTs = it
                            dueText = df.format(Date(it))
                        }
                    }) { Text("Set Due") }
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = {
                        dueTs = null
                        dueText = "Not set"
                    }) { Text("Clear Due") }
                }
                Column {
                    Text("Reminder: $reminderText")
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(onClick = {
                        pickDateTime {
                            reminderTs = it
                            reminderText = df.format(Date(it))
                        }
                    }) { Text("Set Reminder") }
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = {
                        reminderTs = null
                        reminderText = "Not set"
                    }) { Text("Clear Reminder") }
                }
            }
        }
    }, confirmButton = {
        Button(onClick = {
            if (title.isBlank()) return@Button
            onSave(title.trim(), desc.trim(), dueTs, reminderTs)
        }) { Text("Save") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}