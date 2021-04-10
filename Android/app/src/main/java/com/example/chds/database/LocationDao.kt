package com.example.chds.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.chds.data.LocationBubble

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLocation(locationBubble: LocationBubble)

    @Query("SELECT * FROM location_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<LocationBubble>>

    @Update()
    fun updateLocation(locationBubble: LocationBubble)

    @Delete
    fun deleteLocation(locationBubble: LocationBubble)
}