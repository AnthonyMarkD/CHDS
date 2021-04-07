package com.example.chds.geofencing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chds.R
import com.example.chds.data.Location
import com.example.chds.databinding.FragmentGeofencingBinding
import com.example.chds.databinding.FragmentMainBinding

class GeoFencingFragment : Fragment() {
    private var _binding: FragmentGeofencingBinding? = null
    private val binding get() = _binding!!


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
        val adapter = LocationAdapter(emptyList())
        binding.locationsRv.adapter = adapter
        binding.locationsRv.layoutManager = linearLayoutManager

        val model: LocationViewModel by activityViewModels()
        model.getAllLocations.observe(
            this.viewLifecycleOwner,
            Observer<List<Location>>
            { locations ->
                println()
                adapter.setLocationList(locations)
            })


        binding.addLocationFAB.setOnClickListener {
//            val location = Location("Home", false, 1L, 1L)
//
//            model.addLocation(location)
            findNavController().navigate(R.id.action_geoFencingFragment_to_googleMapFragment)
        }
        binding.backBt.setOnClickListener{
            findNavController().popBackStack()
        }
    }
}