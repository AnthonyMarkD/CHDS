package com.example.chds.geofencing

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chds.R
import com.example.chds.data.LocationBubble
import com.example.chds.databinding.BottomsheetSaveLocationBinding
import com.example.chds.databinding.BottomsheetUpdateLocationBinding
import com.example.chds.databinding.FragmentGeofencingBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

class UpdateLocationBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomsheetUpdateLocationBinding? = null
    private val binding get() = _binding!!
    private val model: LocationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = BottomsheetUpdateLocationBinding.inflate(inflater, container, false)

        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.updatedLocation.observe(
            this.viewLifecycleOwner,
            androidx.lifecycle.Observer { location ->
                println(location.lat)
                binding.locationNickNameTv.setText(location.locationName.toString())
                binding.latTv.text = location.lat.toString()
                binding.longTv.text = location.lon.toString()
                binding.radTv.setText(location.radius.toString())
                println(location.id)
            })

        binding.updateLocationBt.setOnClickListener {
            if (binding.locationNickNameTv.text.toString() != "") {
                var locationBubble = model.updatedLocation.value
                if (locationBubble != null) {
                    locationBubble.locationName = binding.locationNickNameTv.text.toString()
                    if (binding.radTv.text.toString() != "") {
                        locationBubble.radius = binding.radTv.text.toString().toDouble()

                        model.updateLocation(locationBubble)
                        if (findNavController().currentDestination?.id != R.id.geoFencingFragment) {
                            model.update = false
                            findNavController().navigate(R.id.action_saveLocationBottomSheet_to_geoFencingFragment)
                        }
                    } else {
                        binding.radTv.error = "Please enter a radius for geo-fencing in meter"
                    }
                }


            } else {
                binding.locationNickNameTv.error = "Please enter a location name"
            }
        }
    }
}