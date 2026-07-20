package com.example.androidfarmerfriend.alerts

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AlertSeedData {

    private val sampleAlerts = listOf(
        mapOf(
            "title" to "Tomato Price Drop",
            "message" to "Tomato price dropped to ₹25/kg in Koyambedu market, down 15% from yesterday.",
            "type" to "PRICE",
            "timestamp" to System.currentTimeMillis() - 3600000,
            "location" to "Koyambedu",
            "isRead" to false,
            "actionRoute" to "market",
            "time" to "1h ago"
        ),
        mapOf(
            "title" to "Onion Price Surge",
            "message" to "Onion price increased to ₹45/kg in Chennai market, up 20% this week.",
            "type" to "PRICE",
            "timestamp" to System.currentTimeMillis() - 7200000,
            "location" to "Chennai",
            "isRead" to false,
            "actionRoute" to "market",
            "time" to "2h ago"
        ),
        mapOf(
            "title" to "Heavy Rain Warning",
            "message" to "Heavy rainfall expected in Namakkal district tomorrow. Secure your crops and ensure proper drainage.",
            "type" to "WEATHER",
            "timestamp" to System.currentTimeMillis() - 10800000,
            "location" to "Namakkal",
            "isRead" to false,
            "actionRoute" to "weather",
            "time" to "3h ago"
        ),
        mapOf(
            "title" to "Storm Alert - Tamil Nadu",
            "message" to "Thunderstorm warning for southern Tamil Nadu. Avoid field work between 2PM-6PM today.",
            "type" to "WEATHER",
            "timestamp" to System.currentTimeMillis() - 14400000,
            "location" to "Tamil Nadu",
            "isRead" to true,
            "actionRoute" to "weather",
            "time" to "4h ago"
        ),
        mapOf(
            "title" to "Leaf Blight Detected",
            "message" to "Leaf blight disease reported in tomato crops near Salem. Use recommended fungicide spray.",
            "type" to "CROP",
            "timestamp" to System.currentTimeMillis() - 21600000,
            "location" to "Salem",
            "isRead" to false,
            "actionRoute" to "disease",
            "time" to "6h ago"
        ),
        mapOf(
            "title" to "Brinjal Fruit Borer Alert",
            "message" to "Fruit borer infestation spotted in brinjal fields in Coimbatore. Use pheromone traps.",
            "type" to "CROP",
            "timestamp" to System.currentTimeMillis() - 43200000,
            "location" to "Coimbatore",
            "isRead" to true,
            "actionRoute" to "disease",
            "time" to "12h ago"
        ),
        mapOf(
            "title" to "Potato Price Update",
            "message" to "Potato wholesale price at ₹18/kg in Bangalore market. Good time to sell stored stock.",
            "type" to "PRICE",
            "timestamp" to System.currentTimeMillis() - 86400000,
            "location" to "Bangalore",
            "isRead" to true,
            "actionRoute" to "market",
            "time" to "Yesterday"
        ),
        mapOf(
            "title" to "Rain Expected - Kerala",
            "message" to "Moderate to heavy rain expected across Kerala for the next 3 days. Plan irrigation accordingly.",
            "type" to "WEATHER",
            "timestamp" to System.currentTimeMillis() - 172800000,
            "location" to "Kerala",
            "isRead" to true,
            "actionRoute" to "weather",
            "time" to "2 days ago"
        ),
        mapOf(
            "title" to "Rice Blast Disease Warning",
            "message" to "Rice blast disease risk high due to humid conditions in Andhra Pradesh. Apply preventive treatment.",
            "type" to "CROP",
            "timestamp" to System.currentTimeMillis() - 259200000,
            "location" to "Andhra Pradesh",
            "isRead" to true,
            "actionRoute" to "disease",
            "time" to "3 days ago"
        ),
        mapOf(
            "title" to "Chili Price Rally",
            "message" to "Green chili prices surged to ₹120/kg in Madurai market. Export demand driving prices up.",
            "type" to "PRICE",
            "timestamp" to System.currentTimeMillis() - 345600000,
            "location" to "Madurai",
            "isRead" to true,
            "actionRoute" to "market",
            "time" to "4 days ago"
        )
    )

    suspend fun seedAlerts() {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        sampleAlerts.forEach { alert ->
            val docRef = db.collection("alerts").document()
            batch.set(docRef, alert)
        }

        batch.commit().await()
    }
}
