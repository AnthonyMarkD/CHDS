package com.example.chds.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_table")
data class Location(
    var locationName: String,
    var enabled: Boolean,
    var lat: Long,
    var lon: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0


}


