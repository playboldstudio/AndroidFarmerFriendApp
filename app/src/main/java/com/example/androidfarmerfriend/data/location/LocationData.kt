package com.example.androidfarmerfriend.data.location

data class SelectedLocation(
    val name: String,
    val lat: Double,
    val lon: Double,
    val marketName: String = name.substringBefore(", ")
)

object Locations {
    val all = listOf(
        SelectedLocation("Namakkal, Tamil Nadu", 11.2189, 78.1674),
        SelectedLocation("Chennai, Tamil Nadu", 13.0827, 80.2707),
        SelectedLocation("Coimbatore, Tamil Nadu", 11.0168, 76.9558),
        SelectedLocation("Madurai, Tamil Nadu", 9.9252, 78.1198),
        SelectedLocation("Salem, Tamil Nadu", 11.6643, 78.1460),
        SelectedLocation("Tiruchirappalli, Tamil Nadu", 10.7905, 78.7047),
        SelectedLocation("Erode, Tamil Nadu", 11.3410, 77.7172),
        SelectedLocation("Thanjavur, Tamil Nadu", 10.7870, 79.1378),
        SelectedLocation("Vellore, Tamil Nadu", 12.9165, 79.1325),
        SelectedLocation("Tirunelveli, Tamil Nadu", 8.7139, 77.7567)
    )

    val default = all.first()
}
