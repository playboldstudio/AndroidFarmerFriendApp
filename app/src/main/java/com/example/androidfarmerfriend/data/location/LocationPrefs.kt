package com.example.androidfarmerfriend.data.location

import android.content.Context
import android.content.SharedPreferences

class LocationPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)

    var selectedLocation: SelectedLocation
        get() {
            val name = prefs.getString(KEY_NAME, null) ?: return Locations.default
            val latStr = prefs.getString(KEY_LAT, null)
            val lonStr = prefs.getString(KEY_LON, null)
            val known = Locations.all.find { it.name == name }
            if (known != null) return known
            if (latStr != null && lonStr != null) {
                val lat = latStr.toDoubleOrNull()
                val lon = lonStr.toDoubleOrNull()
                if (lat != null && lon != null && lat != 0.0 && lon != 0.0) {
                    val marketName = name.substringBefore(", ")
                    return SelectedLocation(name, lat, lon, marketName)
                }
            }
            return Locations.default
        }
        set(value) {
            prefs.edit()
                .putString(KEY_NAME, value.name)
                .putString(KEY_LAT, value.lat.toString())
                .putString(KEY_LON, value.lon.toString())
                .apply()
        }

    companion object {
        private const val KEY_NAME = "location_name"
        private const val KEY_LAT = "location_lat"
        private const val KEY_LON = "location_lon"
    }
}
