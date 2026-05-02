package com.andeshub.utils

import android.location.Location

data class BuildingLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

object LocationUtils {
    private val buildings = listOf(
        BuildingLocation("Mario Laserna", 4.6027, -74.0655),
        BuildingLocation("Santo Domingo", 4.6015, -74.0645),
        BuildingLocation("Centro del Japón", 4.6010, -74.0660),
        BuildingLocation("Biblioteca General", 4.6030, -74.0640),
        BuildingLocation("Cafetería Central", 4.6020, -74.0650)
    )

    /**
     * Retorna el nombre del edificio si el usuario está a menos de 150 metros.
     */
    fun getNearbyBuilding(userLat: Double, userLon: Double): String? {
        val userLocation = Location("").apply {
            latitude = userLat
            longitude = userLon
        }

        return buildings.map { building ->
            val buildingLoc = Location("").apply {
                latitude = building.latitude
                longitude = building.longitude
            }
            building.name to userLocation.distanceTo(buildingLoc)
        }.filter { it.second < 150 } // Umbral de 150 metros
            .minByOrNull { it.second }?.first
    }
}
