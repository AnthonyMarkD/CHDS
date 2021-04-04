package com.example.chds.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_table")
data class Location (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val lat: Long,
    val lon: Long
)

