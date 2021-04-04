package com.example.chds.data

import androidx.lifecycle.LiveData

class LocationRepository(private val locationDao: LocationDao) {
    val readAllData: LiveData<List<Location>> = locationDao.readAllData()

    suspend fun addLocation(location: Location){
        locationDao.addLocation(location)
    }
}