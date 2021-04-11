package com.example.chds.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.chds.R
import com.example.chds.databinding.FragmentBluetoothBinding
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class BluetoothFragment : Fragment() {
    private var _binding: FragmentBluetoothBinding? = null
    private val binding get() = _binding!!
    private val SELECT_DEVICE_REQUEST_CODE = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO Ask for permissions for what ever is needed lol

        binding.backBt.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.searchHapticDevicesBt.setOnClickListener {
            setUpBLE()
        }

        BLEManager.getBLEConnectionStatus().observe(this.viewLifecycleOwner) { connectionStatus ->

            when (connectionStatus) {

                BLEConnectionStatus.Connected -> {
                    binding.hapticDeviceConnectionNameTv.visibility = View.VISIBLE
                    binding.bleMacAddressTv.visibility = View.VISIBLE
                    binding.lastCommandTv.visibility = View.VISIBLE
                    binding.disconnectBLEBt.visibility = View.VISIBLE
                    binding.statusDescription.visibility = View.GONE
                    binding.bleConnectionLottie.visibility = View.GONE
                    binding.searchHapticDevicesBt.visibility = View.GONE
                    val byteArr = BLEManager.byteArrayOfInts(0x56, 0x31)
                    BLEManager.postVibration(byteArr)
                }
                BLEConnectionStatus.NoConnection, BLEConnectionStatus.Disconnecting -> {
                    binding.disconnectBLEBt.visibility = View.GONE
                    binding.lastCommandTv.visibility = View.GONE
                    binding.bleConnectionLottie.visibility = View.VISIBLE
                    binding.statusDescription.visibility = View.VISIBLE
                    binding.hapticDeviceConnectionNameTv.visibility = View.GONE
                    binding.bleMacAddressTv.visibility = View.GONE
                    binding.hapticDeviceConnectionNameTv.text = ""
                    binding.searchHapticDevicesBt.visibility = View.VISIBLE
                }
            }

        }
        BLEManager.getBLEMac().observe(this.viewLifecycleOwner) { macAddress ->
            binding.bleMacAddressTv.text =
                getString(R.string.ble_connected_mac_address) + macAddress
        }
        BLEManager.getBLEDeviceName().observe(this.viewLifecycleOwner) { deviceName ->
            binding.hapticDeviceConnectionNameTv.text =
                getString(R.string.ble_connected_device_name) + deviceName

        }
        BLEManager.getBLELastOperation().observe(this.viewLifecycleOwner) { lastOperation ->

            binding.lastCommandTv.text = getString(R.string.last_ble_command) + lastOperation

        }
        binding.disconnectBLEBt.setOnClickListener {
            BLEManager.disconnect()
        }

    }

    private fun setUpBLE() {
        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            // Match only Bluetooth devices whose name matches the pattern.
            .setNamePattern(Pattern.compile("CHDS"))
            .build()

        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            // Find only devices that match this request filter.
            .addDeviceFilter(deviceFilter)
            // Stop scanning as soon as one device matching the filter is found.
            .setSingleDevice(false)
            .build()
        val deviceManager =
            requireContext().getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager


        deviceManager.associate(
            pairingRequest,
            object : CompanionDeviceManager.Callback() {
                // Called when a device is found. Launch the IntentSender so the user
                // can select the device they want to pair with.
                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    println("Do we reach")
                    startIntentSenderForResult(
                        chooserLauncher,
                        SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0, null
                    )
                }

                override fun onFailure(error: CharSequence?) {
                    // Handle the failure.
                }
            }, null
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println("test")
        when (requestCode) {

            SELECT_DEVICE_REQUEST_CODE -> when (resultCode) {

                Activity.RESULT_OK -> {
                    // The user chose to pair the app with a Bluetooth device.
                    val deviceToPair: BluetoothDevice? =
                        data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                    deviceToPair?.let { device ->
                        // Continue to interact with the paired device.
                        BLEManager.connect(device, requireContext().applicationContext)
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}