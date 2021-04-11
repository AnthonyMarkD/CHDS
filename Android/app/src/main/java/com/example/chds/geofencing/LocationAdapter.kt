package com.example.chds.geofencing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chds.R
import com.example.chds.data.LocationBubble
import com.google.android.material.switchmaterial.SwitchMaterial

class LocationAdapter(
    private var locationsList: List<LocationBubble>,
    private val listener: OnItemClickListener,
    private val switchListener: OnItemClickListener
) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val locationName: TextView = view.findViewById(R.id.locationNameTv)
        val enabled: SwitchMaterial = view.findViewById(R.id.switchEnabled)


        init {
            // Define click listener for the ViewHolder's View.
            view.setOnClickListener {


                listener.onItemClick(adapterPosition)


            }
            enabled.setOnCheckedChangeListener { buttonView, isChecked ->

                switchListener.onSwitchChanged(buttonView, isChecked, adapterPosition)
            }
        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)

        return ViewHolder(view)

    }

//     fun ViewHolder.onBindViewHolder(position: Int) {
//        locationName.text = locationsList[position].locationName
//
//        enabled.isChecked = locationsList[position].enabled
//    }

    override fun getItemCount(): Int {
        return locationsList.size
    }

    fun setLocationList(locationBubbles: List<LocationBubble>) {
        locationsList = locationBubbles
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.locationName.text = locationsList[position].locationName

        holder.enabled.isChecked = locationsList[position].enabled
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onSwitchChanged(buttonView: View, isChecked: Boolean, position: Int)
    }
}







