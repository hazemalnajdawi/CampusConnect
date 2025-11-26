package week11.st573015.finalproject.ui.schedule

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import week11.st573015.finalproject.models.ScheduleItem
import java.util.*

@Composable
fun AddEditScheduleDialog(
    initial: ScheduleItem?,
    onDismiss: () -> Unit,
    onSave: (ScheduleItem) -> Unit,
    onDelete: ((ScheduleItem) -> Unit)? = null
) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var room by remember { mutableStateOf(initial?.room ?: "") }
    var day by remember { mutableStateOf(initial?.dayOfWeek ?: 1) }
    var startMinutes by remember { mutableStateOf(initial?.startMinutes ?: 9 * 60) }
    var endMinutes by remember { mutableStateOf(initial?.endMinutes ?: 10 * 60) }

    val days = listOf(1,2,3,4,5,6,7)
    val dayLabels = mapOf(1 to "Monday",2 to "Tuesday",3 to "Wednesday",4 to "Thursday",5 to "Friday",6 to "Saturday",7 to "Sunday")

    fun pickTime(currentMinutes: Int, onPicked: (Int) -> Unit) {
        val hour = currentMinutes / 60
        val minute = currentMinutes % 60
        TimePickerDialog(ctx, { _, h, m ->
            onPicked(h * 60 + m)
        }, hour, minute, true).show()
    }

    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (initial == null) "Add Class" else "Edit Class") }, text = {
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Class title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            // Day selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                days.forEach { d ->
                    val selected = d == day
                    FilterChip(
                        selected = selected,
                        onClick = { day = d },
                        label = { Text(dayLabels[d]!!.take(3)) },
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { pickTime(startMinutes) { startMinutes = it } }, modifier = Modifier.weight(1f)) {
                    Text("Start: ${formatMinutes(startMinutes)}")
                }
                OutlinedButton(onClick = { pickTime(endMinutes) { endMinutes = it } }, modifier = Modifier.weight(1f)) {
                    Text("End: ${formatMinutes(endMinutes)}")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room / Location") }, modifier = Modifier.fillMaxWidth())
        }
    }, confirmButton = {
        Button(onClick = {
            if (title.isBlank()) return@Button
            // Ensure end > start; if not, swap or adjust
            val s = startMinutes
            var e = endMinutes
            if (e <= s) e = s + 60 // default 1 hour class
            val item = (initial ?: ScheduleItem()).copy(
                title = title.trim(),
                room = room.trim(),
                dayOfWeek = day,
                startMinutes = s,
                endMinutes = e,
                updatedAt = System.currentTimeMillis()
            )
            onSave(item)
        }) { Text("Save") }
    }, dismissButton = {
        Row {
            if (initial != null && onDelete != null) {
                TextButton(onClick = { onDelete(initial) }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            }
            Spacer(modifier = Modifier.width(6.dp))
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    })
}

private fun formatMinutes(minutes: Int): String {
    val hh = minutes / 60
    val mm = minutes % 60
    return String.format(Locale.getDefault(), "%02d:%02d", hh, mm)
}