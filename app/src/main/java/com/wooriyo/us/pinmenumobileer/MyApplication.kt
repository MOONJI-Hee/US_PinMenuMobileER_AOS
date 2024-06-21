package com.wooriyo.us.pinmenumobileer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.graphics.Point
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import com.sam4s.printer.Sam4sBuilder
import com.sewoo.jpos.printer.ESCPOSPrinter
import com.sewoo.port.android.BluetoothPort
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.config.AppProperties
import com.wooriyo.us.pinmenumobileer.model.SharedDTO
import com.wooriyo.us.pinmenumobileer.model.StoreDTO
import com.wooriyo.us.pinmenumobileer.printer.sam4s.PrinterConnection

class MyApplication: Application() {

    //Sam4s 프린터 관련
    lateinit var mPrinterConnection: PrinterConnection

    init {
        INSTANCE = this
    }

    fun getPrinterConnection(): PrinterConnection?{
        return mPrinterConnection
    }

    fun setPrinterConnection(connection: PrinterConnection) {
        mPrinterConnection = connection
    }

    companion object {
        lateinit var INSTANCE: MyApplication

        lateinit var pref: SharedDTO
//        lateinit var db : AppDatabase

        var width = 0
        var height = 0
        var density = 1.0F
        var dpi = 160

        val os = "A"
        var osver = 0
        lateinit var appver : String
        lateinit var md : String

        var useridx = 0
        var storeidx = 0
        lateinit var store: StoreDTO
        lateinit var androidId : String
        val storeList = ArrayList<StoreDTO>()
        var engStoreName = ""

        // 블루투스 관련 변수
        lateinit var bluetoothManager: BluetoothManager
        lateinit var bluetoothAdapter: BluetoothAdapter

        //세우전자 프린터 관련
        lateinit var remoteDevices: ArrayList<BluetoothDevice>  // 페어링 된 기기 중 세우전자 프린터 리스트
        lateinit var bluetoothPort: BluetoothPort
        lateinit var escposPrinter : ESCPOSPrinter
        var btThread: Thread? = null
        var connDev_sewoo = "00:00:00:00:00:00"

        //SAM4S 프린터 관련
        lateinit var cubeBuilder: Sam4sBuilder

        var bidx = 0    //프린터 설정 시 부여되는 idx (기기별 매장 하나 당 한개씩 부여)

        fun setStoreDTO() {
            store = StoreDTO(useridx)
        }
    }

    override fun onCreate() {
        androidId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)

        pref = SharedDTO(applicationContext)
//        db = AppDatabase.getInstance(applicationContext)

        density = resources.displayMetrics.density

        osver = Build.VERSION.SDK_INT
        appver = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionName
        md = Build.MODEL

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            width = windowMetrics.bounds.width()
            height = windowMetrics.bounds.height()
        } else {
            val display = windowManager.defaultDisplay
            val realpoint = Point()
            display.getRealSize(realpoint) // or getSize(size)
            width = realpoint.x
            height = realpoint.y
        }

        //블루투스
        bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        //세우전자
        remoteDevices = ArrayList<BluetoothDevice>()
        bluetoothPort = BluetoothPort.getInstance()
        bluetoothPort.SetMacFilter(false)

        escposPrinter = ESCPOSPrinter()

        //SAM4S
        mPrinterConnection = PrinterConnection(applicationContext, 0)
        cubeBuilder = Sam4sBuilder("GCube-100", Sam4sBuilder.LANG_KO)
        cubeBuilder.addTextSize(1, 1)
        cubeBuilder.addFeedLine(1)
        cubeBuilder.addTextStyle(false, false, false, Sam4sBuilder.COLOR_1)

        createNotificationChannel()

        super.onCreate()
    }

    fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.deleteNotificationChannel(AppProperties.CHANNEL_ID_ORDER)
        notificationManager.deleteNotificationChannel(AppProperties.CHANNEL_ID_ORDER)

        val ordSound = R.raw.customnoti
        val ordUri: Uri = Uri.parse("android.resource://com.wooriyo.us.pinmenumobileer/$ordSound")

        val callSound = R.raw.customcall
        val callUri: Uri = Uri.parse("android.resource://com.wooriyo.us.pinmenumobileer/$callSound")

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        // 주문 알림 채널 생성
        val ordChannel = NotificationChannel(AppProperties.CHANNEL_ID_ORDER, "새 주문 알림", NotificationManager.IMPORTANCE_HIGH)
        ordChannel.enableLights(true)
        ordChannel.enableVibration(true)
        ordChannel.setSound(ordUri, audioAttributes)
        ordChannel.lockscreenVisibility = 1
        notificationManager.createNotificationChannel(ordChannel)

        // 호출 알림 채널 생성
        val callChannel = NotificationChannel(AppProperties.CHANNEL_ID_CALL, "새 호출 알림", NotificationManager.IMPORTANCE_HIGH)
        callChannel.enableLights(true)
        callChannel.enableVibration(true)
        callChannel.setSound(callUri, audioAttributes)
        ordChannel.lockscreenVisibility = 1
        notificationManager.createNotificationChannel(callChannel)
    }
}