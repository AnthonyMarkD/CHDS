package com.example.chds.main

import android.bluetooth.*
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chds.databinding.FragmentMainBinding
import java.util.*
import java.util.regex.Pattern

object BLEManager {
    private val bleConnectionStatus = MutableLiveData<Boolean>()
    private var bluetoothGatt: BluetoothGatt? = null
    private val GATT_MAX_MTU_SIZE = 517

    init {

    }

    fun setUpBluetoothGatt(bluetoothDevice: BluetoothDevice?, context: Context) {
        bluetoothGatt = bluetoothDevice?.connectGatt(context, false, gattCallback)
        Intent(context.applicationContext, BLEScannerService::class.java).also { intent ->
            context.startForegroundService(intent) // Starting BLE scanning service in foreground so system is less likely to kill it
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val deviceAddress = gatt?.device?.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")

                    bluetoothGatt = gatt
                    bluetoothGatt?.discoverServices() // Discoveres BLE services and characteristics
                    bleConnectionStatus.postValue(true)
                    bluetoothGatt?.requestMtu(GATT_MAX_MTU_SIZE) // Request Maximum Transmission unit (MTU)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    bleConnectionStatus.postValue(false)
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt?.close()
                }

            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                bleConnectionStatus.postValue(false)
                gatt?.close()

            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Log.w(
                    "BluetoothGattCallback",
                    "Discovered ${services.size} services for ${device.address}"
                )
                printGattTable() // See implementation just above this section
                // Consider connection setup as complete here
                //Update Ui Here


                getDeviceName()


            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.e(
                "ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}",
                "Oof"
            )
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(
                            "BluetoothGattCallback",
                            "Read characteristic $uuid:\n${value.toHexString()}"
                        )
                        writeToDevice()
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Read not permitted for $uuid!")
                    }
                    else -> {
                        Log.e(
                            "BluetoothGattCallback",
                            "Characteristic read failed for $uuid, error: $status"
                        )
                    }
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i(
                            "BluetoothGattCallback",
                            "Wrote to characteristic $uuid | value: ${value.toHexString()}"
                        )

                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e("BluetoothGattCallback", "Write exceeded connection ATT MTU!")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Write not permitted for $uuid!")
                    }
                    else -> {
                        Log.e(
                            "BluetoothGattCallback",
                            "Characteristic write failed for $uuid, error: $status"
                        )
                    }
                }
            }
        }
    }

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i(
                "printGattTable",
                "No service and characteristic available, call discoverServices() first?"
            )
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { it.uuid.toString() }
            Log.i(
                "printGattTable",
                "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
            )
        }
    }

    fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }

    private fun getDeviceName() {
        val deviceInformationService = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
        val deviceNameCharUUID = UUID.fromString("00002a57-0000-1000-8000-00805f9b34fb")


        val deviceName = bluetoothGatt?.getService(deviceInformationService)
            ?.getCharacteristic(deviceNameCharUUID)


        if (deviceName?.isReadable() == true) {
            bluetoothGatt?.readCharacteristic(deviceName)
        }


    }

    private fun writeToDevice() {
        val deviceInformationService = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
        val deviceNameCharUUID = UUID.fromString("00002a57-0000-1000-8000-00805f9b34fb")


        val deviceName = bluetoothGatt?.getService(deviceInformationService)
            ?.getCharacteristic(deviceNameCharUUID)

        val byteArr = byteArrayOfInts(0x56, 0x31)
        writeCharacteristic(deviceName!!, byteArr)
    }

    fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }

        bluetoothGatt?.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = payload
            gatt.writeCharacteristic(characteristic)
        } ?: error("Not connected to a BLE device!")
    }

    fun getBLEConnectionStatus(): LiveData<Boolean> = bleConnectionStatus as LiveData<Boolean>
}