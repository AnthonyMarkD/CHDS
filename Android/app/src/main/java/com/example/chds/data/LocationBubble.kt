package com.example.chds.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.versionedparcelable.ParcelField

@Entity(tableName = "location_table")
data class LocationBubble(
    var locationName: String,
    var enabled: Boolean,
    var lat: Double,
    var lon: Double,
    var radius: Double
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0


}


