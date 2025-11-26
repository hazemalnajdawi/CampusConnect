package week11.st573015.finalproject.notifications

import android.app.Application
import android.content.Context

/**
 * Simple holder to get Application context in non-Android classes (ReminderScheduler).
 * You must initialize this in your Application class or MainActivity onCreate:
 * NotificationContext.init(application) or NotificationContext.applicationContext = application
 */
object NotificationContext {
    var applicationContext: Context? = null

    fun init(app: Application) {
        applicationContext = app.applicationContext
    }
}