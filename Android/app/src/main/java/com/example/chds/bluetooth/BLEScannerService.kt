package com.example.chds.bluetooth

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import com.example.chds.R
import com.example.chds.main.MainActivity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.sqrt

class BLEScannerService : Service() {
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var scanning = false
    private var distances = ArrayList<Double>()
    private val period = 5
    private val numStd = 1
    private var movingAverage: Double = 0.0
    private var movingVariance: Double = 0.0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Service:", "OnStartCalled")
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner!!
        val notification = createForegroundNotification()
        startForeground(1, notification)
        BLEManager.getBLEConnectionStatus().observeForever { status ->
            when (status) {
                // Connected
                BLEConnectionStatus.NoConnection, BLEConnectionStatus.Connected -> {
                    // Want to start a connection, let the service run
                }

                BLEConnectionStatus.Disconnecting -> {
                    // Kill Service
                    bluetoothLeScanner.stopScan(leScanCallback)
                    BLEManager.bleConnectionStatus.postValue(BLEConnectionStatus.NoConnection)
                    stopForeground(true)
                    stopSelf()
                }
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

            val point = result.rssi.toDouble()
            // keep these line below
            val n = distances.size
            distances.add(point)

            when {
                n == 0 -> {
                    // initial value
                    movingAverage = distances[0]
                    movingVariance = 0.0
                }
                n < period -> {
                    // filling of the buffer
                    var sumP = 0.0
                    for (i in 0..n) {
                        sumP = distances[i]
                    }

                    movingAverage = sumP / (n + 1)

                    var sumPa2 = 0.0
                    for (i in 0..n) {
                        sumPa2 += (distances[i] - movingAverage) * (distances[i] - movingAverage)
                    }

                    movingVariance = sumPa2 / (n + 1)
                }
                else -> {
                    // actual calculation for when n > PERIOD
                    val deltaA = (point - distances[n - period]) / period
                    movingAverage += deltaA
                    movingAverage += deltaA * (deltaA + point + distances[n - period] - 2 * movingAverage)
                }
            }


            // Check if it's out of range
            val stdDev = sqrt(movingVariance);
            val rssi = when {
                point > (movingAverage + numStd * stdDev) -> {
                    // It's an outlier, above a standard deviation
                    movingAverage + numStd * stdDev
                }
                point < (movingAverage - numStd * stdDev) -> {
                    // It's an outlier, below a standard deviation
                    movingAverage - numStd * stdDev
                }
                else -> {
                    // It's a reasonable measurement
                    // Replace the line below
                    point
                }
            }
            val distance = calculateRSSIDistance(rssi)


            //TODO Distance vibration check

            println(result.rssi)
            println(calculateRSSIDistance(result.rssi.toDouble()))
            println(result.scanRecord?.serviceUuids)
            println(result.device.name)

//            leDeviceListAdapter.addDevice(result.device)
//            leDeviceListAdapter.notifyDataSetChanged()
        }
    }

    private fun scanLeDevice() {
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString("0000180a-0000-1000-8000-00805f9b34fb"))
            .build()
        val filter2 = ScanFilter.Builder()
            .setDeviceName("CHDS")
            .build()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()
        val filterList = mutableListOf<ScanFilter>()
        filterList.add(filter)
        filterList.add(filter2)
        bluetoothLeScanner.let { scanner ->


            scanning = true
            scanner.startScan(filterList, scanSettings, leScanCallback)

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
//        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show();

    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH
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

    private fun calculateRSSIDistance(RSSIMeasurement: Double): Double {
        val A =
            -62.0 // RSSI value with Samsung Galaxy S20 Plus as testing device at a distance of 1m from MKR WIFI 1010 chip.
        val n = 2.0 // path loss index in specific enviroment
        println(((A - RSSIMeasurement) / (10 * n)))
        return 10.00.pow((((A - RSSIMeasurement) / (10 * n))))
    }
}