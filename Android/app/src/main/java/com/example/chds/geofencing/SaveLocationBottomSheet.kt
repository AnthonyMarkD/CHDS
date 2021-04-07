package com.example.chds.geofencing

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.example.chds.databinding.BottomsheetSaveLocationBinding
import com.example.chds.databinding.FragmentGeofencingBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

class SaveLocationBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomsheetSaveLocationBinding? = null
    private val binding get() = _binding!!
    private val model: LocationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomsheetSaveLocationBinding.inflate(inflater, container, false)

        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.selectedLocation.observe(
            this.viewLifecycleOwner,
            androidx.lifecycle.Observer { location ->
                println(location.lat)
                binding.latTv.text = location.lat.toString()
                binding.longTv.text = location.lon.toString()
            })

        binding.saveLocationBt.setOnClickListener {
            binding.locationNickNameTv.error = "Please stop being dumb"
        }
    }
}