package week11.st573015.finalproject.models

data class ScheduleItem(
    val id: String = "",
    val title: String = "",
    val dayOfWeek: Int = 1, // 1=Monday .. 7=Sunday (use 1..7 to match Calendar)
    val startMinutes: Int = 9 * 60, // minutes since midnight (e.g., 9*60 = 9:00)
    val endMinutes: Int = 10 * 60,  // minutes since midnight
    val room: String = "",
    val locationId: String? = null, // optional link to a saved campus location
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)