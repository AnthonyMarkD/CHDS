package com.example.chds.database

import androidx.lifecycle.LiveData
import com.example.chds.data.LocationBubble

class LocationRepository(private val locationDao: LocationDao) {
    val readAllData: LiveData<List<LocationBubble>> = locationDao.readAllData()

    suspend fun addLocation(locationBubble: LocationBubble){
        locationDao.addLocation(locationBubble)
    }
    fun updateLocation(locationBubble: LocationBubble){
        locationDao.updateLocation(locationBubble)
    }
    fun deleteLocation(locationBubble: LocationBubble){
        locationDao.deleteLocation(locationBubble)
    }
}