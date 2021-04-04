package com.example.chds.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import java.util.*


//Referenced https://punchthrough.com/android-ble-guide/
sealed class BLEOperationType {
    abstract val bleDevice: BluetoothDevice
}

data class connectToDevice(override val bleDevice: BluetoothDevice, val context: Context) :
    BLEOperationType()


data class disconnectFromDevice(override val bleDevice: BluetoothDevice) : BLEOperationType()

data class writeToCharacteristic(
    override val bleDevice: BluetoothDevice,
    val characteristic: BluetoothGattCharacteristic,
    val data: ByteArray
) : BLEOperationType() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as writeToCharacteristic

        if (bleDevice != other.bleDevice) return false
        if (characteristic != other.characteristic) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bleDevice.hashCode()
        result = 31 * result + characteristic.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

data class readFromCharacteristic(
    override val bleDevice: BluetoothDevice,
    val characteristic: BluetoothGattCharacteristic
) : BLEOperationType()
