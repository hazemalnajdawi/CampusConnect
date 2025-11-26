package week11.st573015.finalproject.models

data class LocationPin(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)