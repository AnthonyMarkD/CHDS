package com.example.chds.main

import android.app.Activity
import android.bluetooth.*
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Parcel
import android.os.ParcelUuid
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chds.R
import com.example.chds.databinding.FragmentMainBinding
import java.util.*
import java.util.regex.Pattern


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // create the view and inflate it

        _binding = FragmentMainBinding.inflate(inflater, container, false)

        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // view created
        binding.cardBle.setOnClickListener {
            // Connect to BLE
            findNavController().navigate(R.id.action_mainFragment_to_bluetoothFragment)
        }
        binding.cardGeofence.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_geoFencingFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
