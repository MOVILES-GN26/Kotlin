package com.andeshub.utils

import android.location.Location

data class BuildingLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

object LocationUtils {
    val buildings = listOf(
        BuildingLocation("Mario Laserna", 4.60273, -74.06550),
        BuildingLocation("Santo Domingo", 4.60155, -74.06450),
        BuildingLocation("Centro del Japón", 4.60100, -74.06600),
        BuildingLocation("Biblioteca General", 4.60300, -74.06400),
        BuildingLocation("Cafetería Central", 4.60200, -74.06500)
    )

    fun getBuildingCoordinates(name: String): BuildingLocation? {
        // Buscamos coincidencia exacta o contenida (ej: "Edificio Mario Laserna" -> "Mario Laserna")
        return buildings.find { 
            name.contains(it.name, ignoreCase = true) || it.name.contains(name, ignoreCase = true)
        }
    }

    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

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
        }.filter { it.second < 200 } // Aumentamos un poco el rango a 200m
            .minByOrNull { it.second }?.first
    }
}
