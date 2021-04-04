package com.example.chds.geofencing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.chds.data.Location
import com.example.chds.database.LocationDatabase
import com.example.chds.database.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationViewModel(application: Application): AndroidViewModel(application) {
    val getAllLocations: LiveData<List<Location>>
    private val repository: LocationRepository

    init{
        val locationDao = LocationDatabase.getDatabase(application).locationDao()
        repository = LocationRepository(locationDao)
        getAllLocations = repository.readAllData
    }

    fun addLocation(location: Location){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addLocation(location)
        }
    }
}