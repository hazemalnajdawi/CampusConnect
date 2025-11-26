package week11.st573015.finalproject.models

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueTimestamp: Long? = null,
    val reminderTimestamp: Long? = null,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
