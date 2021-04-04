package com.example.chds.geofencing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chds.R
import com.example.chds.data.Location
import com.google.android.material.switchmaterial.SwitchMaterial

class LocationAdapter(private var locationsList: List<Location>) :
    RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val locationName: TextView = view.findViewById(R.id.locationNameTv)
        val enabled: SwitchMaterial = view.findViewById(R.id.switchEnabled)

        init {
            // Define click listener for the ViewHolder's View.
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)

        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: LocationAdapter.ViewHolder, position: Int) {
        holder.locationName.text = locationsList[position].locationName

        holder.enabled.isChecked = locationsList[position].enabled
    }

    override fun getItemCount(): Int {
        return locationsList.size
    }

    fun setLocationList(locations: List<Location>) {
        locationsList = locations
        notifyDataSetChanged()
    }
}