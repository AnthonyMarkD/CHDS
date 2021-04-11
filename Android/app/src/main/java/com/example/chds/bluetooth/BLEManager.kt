package com.example.chds.bluetooth

import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chds.bluetooth.BLEManager.toHexString
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

object BLEManager {
    val bleConnectionStatus = MutableLiveData<BLEConnectionStatus>()
    private val bleDeviceName = MutableLiveData<String>()
    private val bleMacAddress = MutableLiveData<String>()
    private var bluetoothGatt: BluetoothGatt? = null
    private val GATT_MAX_MTU_SIZE = 517
    private val bleOperationQueue =
        ConcurrentLinkedQueue<BLEOperationType>() // Thread safe FIFO queue
    private var pendingOperation: BLEOperationType? = null
    val deviceInformationService = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
    val deviceNameCharUUID = UUID.fromString("00002a57-0000-1000-8000-00805f9b34fb")
    var lastOperation = MutableLiveData<String>()

    fun connect(device: BluetoothDevice, context: Context) {
        enqueueOperation(connectToDevice(device, context.applicationContext))
    }

    fun disconnect() {
        if (bluetoothGatt?.device != null) {
            enqueueOperation(disconnectFromDevice(bluetoothGatt!!.device))
        }

    }

    fun writeToDeviceCharacteristic(
        device: BluetoothDevice,
        serviceUUID: UUID,
        characteristicUUID: UUID,
        data: ByteArray
    ) {

        val characteristic = bluetoothGatt?.getService(serviceUUID)
            ?.getCharacteristic(characteristicUUID)

        if (characteristic?.isWritable() == true) {

            enqueueOperation(writeToCharacteristic(device, characteristic, data))
        }
    }

    fun readCharacteristic(device: BluetoothDevice, serviceUUID: UUID, characteristicUUID: UUID) {


        val characteristic = bluetoothGatt?.getService(serviceUUID)
            ?.getCharacteristic(characteristicUUID)


        if (characteristic?.isReadable() == true) {

            enqueueOperation(readFromCharacteristic(device, characteristic))
        }

    }

    fun postVibration(byteArray: ByteArray) {

        val deviceInformationService = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
        val deviceNameCharUUID = UUID.fromString("00002a57-0000-1000-8000-00805f9b34fb")

        bluetoothGatt?.device?.let {


            writeToDeviceCharacteristic(
                it,
                deviceInformationService,
                deviceNameCharUUID,
                byteArray
            )

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

                    //bluetoothGatt?.requestMtu(GATT_MAX_MTU_SIZE) // Request Maximum Transmission unit (MTU)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    bleConnectionStatus.postValue(BLEConnectionStatus.Disconnecting)
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt?.close()
                }

            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                bleConnectionStatus.postValue(BLEConnectionStatus.Disconnecting)
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
                if (pendingOperation is connectToDevice) {
                    // Consider that the connection setup is now complete
                    bleConnectionStatus.postValue(BLEConnectionStatus.Connected)
                    bleMacAddress.postValue(gatt.device?.address)
                    bleDeviceName.postValue(gatt.device?.name)
                    signalEndOfOperation()
                }


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
                            "Read characteristic $uuid: ${value.toHexString()}"
                        )

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
                if (pendingOperation is readFromCharacteristic) {
                    // Consider that the connection setup is now complete
                    lastOperation.postValue(
                        "Read characteristic $uuid: " +
                                value.toHexString()
                    )
                    signalEndOfOperation()
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
                            "Wrote to characteristic $uuid value: ${value.toHexString()}"
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
                if (pendingOperation is writeToCharacteristic) {
                    // Consider that the connection setup is now complete
                    lastOperation.postValue(
                        "Wrote to characteristic $uuid value: ${value.toHexString()}"
                    )
                    signalEndOfOperation()
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

    @Synchronized
    private fun enqueueOperation(operation: BLEOperationType) {
        bleOperationQueue.add(operation)
        println("Adding in operation")
        if (pendingOperation == null) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun doNextOperation() {
        if (pendingOperation != null) {
            Log.e(
                "ConnectionManager",
                "doNextOperation() called when an operation is pending! Aborting."
            )
            return
        }

        val operation = bleOperationQueue.poll() ?: run {
            Log.d("BLE Manager", "Operation queue empty, returning")
            return
        }
        pendingOperation = operation
        print("Doing next operation")


        when (operation) {
            is connectToDevice -> {
                with(operation) {

                    bleDevice.connectGatt(context, false, gattCallback)
                    Intent(
                        context.applicationContext,
                        BLEScannerService::class.java
                    ).also { intent ->
                        //TODO Start Service but Connected can still be false if disconnected earlier
                        context.startForegroundService(intent) // Starting BLE scanning service in foreground so system is less likely to kill it
                    }

                }
            }
            is disconnectFromDevice -> {
                bleConnectionStatus.postValue(BLEConnectionStatus.Disconnecting)
                bluetoothGatt?.close()
                bleDeviceName.postValue("")
                bleMacAddress.postValue("")
                lastOperation.postValue("")

                signalEndOfOperation()
            }
            is writeToCharacteristic -> {
                with(operation) {
                    val writeType = when {
                        characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        characteristic.isWritableWithoutResponse() -> {
                            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                        }
                        else -> error("Characteristic ${characteristic.uuid} cannot be written to")
                    }
                    bluetoothGatt?.let { gatt ->
                        characteristic.writeType = writeType
                        characteristic.value = data
                        gatt.writeCharacteristic(characteristic)

                    } ?: error("Not connected to a BLE device!")
                }


            }
            is readFromCharacteristic -> {
                with(operation) {
                    bluetoothGatt?.readCharacteristic(characteristic)
                }

            }

        }
    }

    @Synchronized
    private fun signalEndOfOperation() {
        Log.d("ConnectionManager", "End of $pendingOperation")
        pendingOperation = null

        if (bleOperationQueue.isNotEmpty()) {

            doNextOperation()
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


    fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }


    fun getBLEConnectionStatus(): LiveData<BLEConnectionStatus> = bleConnectionStatus
    fun getBLEDeviceName(): LiveData<String> = bleDeviceName
    fun getBLELastOperation(): LiveData<String> = lastOperation
    fun getBLEMac(): LiveData<String> = bleMacAddress
}