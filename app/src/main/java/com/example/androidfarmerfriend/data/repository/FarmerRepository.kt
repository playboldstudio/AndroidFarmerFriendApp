package com.example.androidfarmerfriend.data.repository

import com.example.androidfarmerfriend.data.model.*

class FarmerRepository {
    fun getCrops(): List<Crop> = listOf(
        Crop(1, "தக்காளி", "₹28 / kg", 4.2, "காய்கறிகள்"),
        Crop(2, "வெங்காயம்", "₹22 / kg", -1.3, "காய்கறிகள்"),
        Crop(3, "மிளகாய்", "₹60 / kg", 2.1, "காய்கறிகள்"),
        Crop(4, "உருளைக்கிழங்கு", "₹18 / kg", -0.5, "காய்கறிகள்"),
        Crop(5, "கத்தரிக்காய்", "₹32 / kg", 1.8, "காய்கறிகள்")
    )

    fun getWeather(): WeatherInfo = WeatherInfo(
        temperature = "32°C",
        condition = "பகுதி மேகம்",
        humidity = "65%",
        windSpeed = "12 km/h",
        windDirection = "SW",
        rainChance = "20%",
        location = "Namakkal, Tamil Nadu"
    )

    fun getAlerts(): List<Alert> = listOf(
        Alert(1, "விலை அலர்ட்", "தக்காளி விலை ₹30/kg ஆக உயர்ந்துள்ளது", "2 மணி நேரத்திற்கு முன்", AlertType.PRICE),
        Alert(2, "வானிலை அலர்ட்", "நாளை கனமழைக்கு வாய்ப்பு - Namakkal மாவட்டத்தில்", "1 மணி நேரத்திற்கு முன்", AlertType.WEATHER),
        Alert(3, "பயிர் அலர்ட்", "இலை கருகல் நோய் பரவ வாய்ப்பு - தக்காளி பயிரில் கவனம் தேவை", "2 மணி நேரத்திற்கு முன்", AlertType.CROP)
    )
    
    fun getSchemes(): List<Scheme> = listOf(
        Scheme(1, "பிரதான் மந்திரி கிசான் திட்டம்", "சிறு மற்றும் குறு விவசாயிகளுக்கு ஆண்டுக்கு ரூ. 6000", "மத்திய அரசு"),
        Scheme(2, "உழவர் காப்பீட்டு திட்டம்", "விவசாயிகளுக்கான பயிர் இழப்பு ஈடு செய்யும் திட்டம்", "மாநில அரசு"),
        Scheme(3, "பயண உதவி திட்டம்", "விவசாயப் பொருட்களை சந்தைக்கு கொண்டு செல்ல மானியம்", "மாநில அரசு"),
        Scheme(4, "விதை மானியம் திட்டம்", "உயர் விளைச்சல் ரக விதைகள் மானிய விலையில்", "மத்திய அரசு")
    )

    fun getDiseases(): List<Disease> = listOf(
        Disease(1, "இலை கருகல் நோய்", "தக்காளி"),
        Disease(2, "பூஞ்சை நோய்", "வெங்காயம்"),
        Disease(3, "பழு சிதைவு", "மிளகாய்"),
        Disease(4, "வேர் அழுகல்", "நிலக்கடலை"),
        Disease(5, "மஞ்சள் வைரஸ்", "பயறு")
    )
}
