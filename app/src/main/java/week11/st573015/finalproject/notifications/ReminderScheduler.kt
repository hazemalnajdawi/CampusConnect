package week11.st573015.finalproject.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import week11.st573015.finalproject.models.Task

object ReminderScheduler {

    private fun requestCodeForTask(task: Task): Int = task.id.hashCode()

    fun scheduleReminderForTask(task: Task) {
        val ctx = NotificationContext.applicationContext ?: return
        val reminderTs = task.reminderTimestamp ?: return
        if (reminderTs < System.currentTimeMillis()) return // don't schedule past reminders

        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, task.title)
        }
        val pending = PendingIntent.getBroadcast(
            ctx,
            requestCodeForTask(task),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTs, pending)
    }

    fun cancelReminderForTask(task: Task) {
        val ctx = NotificationContext.applicationContext ?: return
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(ctx, requestCodeForTask(task), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (pending != null) {
            am.cancel(pending)
            pending.cancel()
        }
    }
}