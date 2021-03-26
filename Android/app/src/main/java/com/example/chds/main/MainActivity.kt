package com.example.chds.main

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chds.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val fragment = MainFragment()
            // FragmentManager adds fragment to frame layout
            supportFragmentManager
                .beginTransaction()
                .add(R.id.main_content, fragment)
                .commit()
        }
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
//        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            //startActivityForResult(enableBtIntent, 1)
//        }
        Intent(this, BLEScannerService::class.java).also { intent ->
            startForegroundService(intent) // Starting BLE scanning service in foreground so system is less likely to kill it
        }

    }
}