package com.example.chds.bluetooth

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import com.example.chds.R
import com.example.chds.main.MainActivity
import java.util.*

class BLEScannerService : Service() {
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 1000000
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Service:", "OnStartCalled")
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner!!

        val notification = createForegroundNotification()
        startForeground(1, notification)
        //TODO grab BLEManager values and listen to them
        BLEManager.getBLEConnectionStatus().observeForever { status ->
            if (status) {
                // Connected

                Log.d("Service:", "connected = true")
            } else {
                //Disconnected

                bluetoothLeScanner.stopScan(leScanCallback)

                Log.d("Service:", "connected = false")
                stopForeground(true)
                stopSelf()


            }
        }

        // Initializes Bluetooth adapter.


        scanLeDevice()
        return START_NOT_STICKY


    }


    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            if (result.device.name != null) {
                println(result.device.name)
                println(result.rssi)
            }

//            leDeviceListAdapter.addDevice(result.device)
//            leDeviceListAdapter.notifyDataSetChanged()
        }
    }

    private fun scanLeDevice() {
        val filter = ScanFilter.Builder()
            .setDeviceName("CHDS").build()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()
        val filterList = mutableListOf<ScanFilter>()
        filterList.add(filter)
        bluetoothLeScanner?.let { scanner ->


            scanning = true
            scanner.startScan(filterList,scanSettings,leScanCallback)

        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {

        // Service will not be binded to
        return null
    }

    override fun onDestroy() {
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show();

    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_LOW
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun createForegroundNotification(): Notification {
        val channelId =
            createNotificationChannel("CHDS_NOTIF_CHANNEL_0", "CHDS BLE FOREGROUND SCANNING SERVER")

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        return Notification.Builder(this, channelId)
            .setContentTitle("BLE scanning enabled")
            .setContentText("Scanning stuff")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker("BLE scanning is now in the foreground") //Used by accessibility services to create an audible announcement of notification's purpose.
            .build()
    }
}