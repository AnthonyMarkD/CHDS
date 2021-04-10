package com.example.chds.geofencing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chds.data.LocationBubble
import com.example.chds.database.LocationDatabase
import com.example.chds.database.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    val getAllLocations: LiveData<List<LocationBubble>>
    val selectedLocation = MutableLiveData<LocationBubble>()
    val updatedLocation = MutableLiveData<LocationBubble>()
    var update = false

    private val repository: LocationRepository

    init {
        val locationDao = LocationDatabase.getDatabase(application).locationDao()
        repository = LocationRepository(locationDao)
        getAllLocations = repository.readAllData
    }

    fun setCurrentLocation(locationBubble: LocationBubble) {
        selectedLocation.value = locationBubble
    }

    fun setUpdatedLocation(locationBubble: LocationBubble){
        updatedLocation.value = locationBubble
    }

    fun addLocation(locationBubble: LocationBubble) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addLocation(locationBubble)
        }
    }

    fun updateLocation(locationBubble: LocationBubble) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLocation(locationBubble)
        }
    }
    fun deleteLocation(locationBubble: LocationBubble) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLocation(locationBubble)
        }
    }
}