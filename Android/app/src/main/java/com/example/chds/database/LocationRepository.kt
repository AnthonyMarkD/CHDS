package com.example.chds.database

import androidx.lifecycle.LiveData
import com.example.chds.data.Location

class LocationRepository(private val locationDao: LocationDao) {
    val readAllData: LiveData<List<Location>> = locationDao.readAllData()

    suspend fun addLocation(location: Location){
        locationDao.addLocation(location)
    }
}