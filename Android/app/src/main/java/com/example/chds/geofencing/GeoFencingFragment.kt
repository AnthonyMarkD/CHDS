package com.example.chds.geofencing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chds.R
import com.example.chds.data.LocationBubble
import com.example.chds.databinding.FragmentGeofencingBinding
import com.google.android.material.switchmaterial.SwitchMaterial

class GeoFencingFragment : Fragment(), LocationAdapter.OnItemClickListener {
    private var _binding: FragmentGeofencingBinding? = null
    private val binding get() = _binding!!
    private val model: LocationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGeofencingBinding.inflate(inflater, container, false)

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val model:  by viewModels()
//        model.getUsers().observe(this, Observer<List<User>>{ users ->
//            // update UI
//        })
        val linearLayoutManager =
            LinearLayoutManager(requireContext())
        val adapter = LocationAdapter(emptyList(), this, this)
        binding.locationsRv.adapter = adapter
        binding.locationsRv.layoutManager = linearLayoutManager

        val model: LocationViewModel by activityViewModels()
        model.getAllLocations.observe(
            this.viewLifecycleOwner,
            Observer<List<LocationBubble>>
            { locations ->
                println()
                adapter.setLocationList(locations)
            })

        binding.addLocationFAB.setOnClickListener {
            findNavController().navigate(R.id.action_geoFencingFragment_to_googleMapFragment)
        }
        binding.backBt.setOnClickListener {
            findNavController().popBackStack()
        }

//        val enabled: SwitchMaterial = view.findViewById(R.id.switchEnabled)
//        enabled.setOnClickListener{
//
//        }
    }

    override fun onItemClick(position: Int) {

        val clickedLocation = model.getAllLocations.value?.get(position)
        if (clickedLocation != null) {
            model.setUpdatedLocation(clickedLocation)
            model.update = true
            findNavController().navigate(R.id.action_geoFencingFragment_to_googleMapFragment)
        }

    }

    override fun onSwitchChanged(buttonView: View, isChecked: Boolean, position: Int) {
        val location = model.getAllLocations.value?.get(position)
        if (location != null) {
            location.enabled = isChecked
            model.updateLocation(location)
        }

    }
}
