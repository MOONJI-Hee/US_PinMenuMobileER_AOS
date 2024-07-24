package com.wooriyo.us.pinmenumobileer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.bean.LableSizeBean
import com.rt.printerlibrary.cmd.EscFactory
import com.rt.printerlibrary.connect.PrinterInterface
import com.rt.printerlibrary.enumerate.CommonEnum
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum
import com.rt.printerlibrary.enumerate.SettingEnum
import com.rt.printerlibrary.factory.cmd.CmdFactory
import com.rt.printerlibrary.factory.connect.BluetoothFactory
import com.rt.printerlibrary.factory.connect.PIFactory
import com.rt.printerlibrary.factory.printer.PrinterFactory
import com.rt.printerlibrary.observer.PrinterObserver
import com.rt.printerlibrary.observer.PrinterObserverManager
import com.rt.printerlibrary.printer.RTPrinter
import com.rt.printerlibrary.printer.ThermalPrinter
import com.rt.printerlibrary.setting.CommonSetting
import com.rt.printerlibrary.setting.TextSetting
import com.rt.printerlibrary.utils.FuncUtils
import com.rt.printerlibrary.utils.PrintStatusCmd
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.rtPrinter
import com.wooriyo.us.pinmenumobileer.databinding.ActivityPrinterBinding
import java.io.IOException
import java.io.UnsupportedEncodingException

class PrinterActivity : BaseActivity(), OnClickListener, PrinterObserver {
    lateinit var binding: ActivityPrinterBinding
    lateinit var thread: Thread
    lateinit var pairedDevices : List<BluetoothDevice>

    val foundDevices = ArrayList<BluetoothDevice>()

    var isSearched = false

    var mBluetoothReceiver : BroadcastReceiver ?= null
    var mBluetoothIntentFilter : IntentFilter ?= null

    var printerFactory: PrinterFactory? = null
    val printStatusCmd: PrintStatusCmd = PrintStatusCmd.cmd_print_100402
    var curPrinterInterface: PrinterInterface<*> ?= null
    var configObj : Any ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrinterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        thread = Thread(Runnable{
            val reVal = getPairedDevice()
            if(reVal == 1) {
                val list = StringBuffer()
                pairedDevices.forEach{
                    list.append("${it.name} ${it.address} ${it.uuids}\n")
                }
                binding.list.text = list
            }else {
                binding.list.text = "없다!!!!!!"
            }
        })
        thread.start()

        binding.search.setOnClickListener(this@PrinterActivity)
        binding.connect.setOnClickListener(this@PrinterActivity)
        binding.print.setOnClickListener(this@PrinterActivity)


        // init
//        MyApplication.INSTANCE.setCurrentCmdType(BaseEnum.CMD_ESC)
//        printerFactory = UniversalPrinterFactory()
//        rtPrinter = printerFactory!!.create()
        PrinterObserverManager.getInstance().add(this)

        rtPrinter.setPrinterInterface(curPrinterInterface)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.search -> {
                if (!MyApplication.bluetoothAdapter.isEnabled) {
                    Toast.makeText(mActivity, "블루투스 사용 못함", Toast.LENGTH_SHORT).show()
                } else {
                    binding.search.isEnabled = false
                    loadingDialog.show(supportFragmentManager)

                    binding.list.text = ""

                    if(isSearched) {
                        foundDevices.clear()
                    }else {
                        mBluetoothReceiver = object : BroadcastReceiver() {
                            override fun onReceive(context: Context, intent: Intent) {
                                val action = intent.action
                                // When discovery finds a device
                                when (action) {
                                    BluetoothDevice.ACTION_FOUND -> {
                                        // Get the BluetoothDevice object from the Intent
                                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                                        val devType = device!!.getBluetoothClass().majorDeviceClass
                                        if (devType != BluetoothClass.Device.Major.IMAGING) { // 1536
                                            return
                                        }
                                        if(!foundDevices.contains(device)) {
                                            foundDevices.add(device)
                                        }
                                    }
                                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                                        MyApplication.bluetoothAdapter.cancelDiscovery()
                                        context.unregisterReceiver(mBluetoothReceiver)
                                        loadingDialog.dismiss()
                                        binding.search.isEnabled = true

                                        val list = StringBuffer()
                                        if(foundDevices.isNotEmpty()){
                                            foundDevices.forEach{
                                                list.append("${it.name} ${it.address} ${it.uuids}\n")
                                            }
                                            binding.list.text = list

                                            configObj = BluetoothEdrConfigBean(foundDevices[0])
//                                            isConfigPrintEnable(configObj)
                                        }


                                    }
                                }
                            }
                        }

                        mBluetoothIntentFilter = IntentFilter()
                        mBluetoothIntentFilter?.addAction(BluetoothDevice.ACTION_FOUND)
                        mBluetoothIntentFilter?.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

                        isSearched = true
                    }
                    registerReceiver(mBluetoothReceiver, mBluetoothIntentFilter)
                    MyApplication.bluetoothAdapter.startDiscovery()
                }
            }
            binding.connect -> {
                loadingDialog.show(supportFragmentManager)

//                TimeRecordUtils.record("RT连接start：", System.currentTimeMillis());
                val bluetoothEdrConfigBean : BluetoothEdrConfigBean = configObj as BluetoothEdrConfigBean
                connectBluetooth(bluetoothEdrConfigBean);



//                registerReceiver(object : BroadcastReceiver(){
//                    override fun onReceive(context: Context?, intent: Intent?) {
//
//                        loadingDialog.dismiss()
//                        Toast.makeText(mActivity, "BluetoothConnect Success", Toast.LENGTH_SHORT).show()
//
////                        startActivity(Intent(mActivity, ByHistoryActivity::class.java))
//                    }
//                }, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))

//                if(foundDevices.isNotEmpty())
//                    connDevice(foundDevices[0])

//                if(foundDevices.isNotEmpty()) {
//                    val connDvc = foundDevices[0]
//
//                    try {
//                        MyApplication.bluetoothPort.connect(connDvc)
//                    } catch (e: IOException) {
//                        loadingDialog.dismiss()
//                        e.printStackTrace()
//                        Log.d(TAG, "Connect FAil > $e")
//                    }
//                }else {
//                    loadingDialog.dismiss()
//                    Log.d(TAG, "FoundDevices Empty!!")
//                }
            }
            binding.print -> {
                print()
            }
        }
    }

fun connectBluetooth(bluetoothEdrConfigBean: BluetoothEdrConfigBean) {
    val piFactory: PIFactory = BluetoothFactory()
    val printerInterface = piFactory.create() as PrinterInterface
//    printerInterface.configObject = bluetoothEdrConfigBean
    printerInterface.configObject = configObj
    rtPrinter.setPrinterInterface(printerInterface)
    try {
        (rtPrinter as ThermalPrinter ).connect(configObj)
    } catch (e: Exception) {
        e.printStackTrace()
        Log.d(TAG, "Connect Error > $e")
    }
}


    fun getPairedDevice() : Int {
        Log.d(TAG, "getPairedDevice 시작")

        pairedDevices = MyApplication.bluetoothAdapter.bondedDevices.toList()
//        remoteDevices?.forEach { device ->
//
//            val deviceHardwareAddress = device.address // MAC address
//
//            if(MyApplication.bluetoothPort.isValidAddress(deviceHardwareAddress)) {
//                val deviceNum = device.bluetoothClass.majorDeviceClass
//            }
//        }
        Log.d(TAG, "페어링된 기기 목록 >>${pairedDevices}")

        return if(pairedDevices.isNotEmpty()) 1 else 0
    }

    fun connDevice(connDvc: BluetoothDevice) {
        Log.d("AppHelper", "connDvc >> $connDvc")
//
//        try {
//            val bluetoothEdrConfigBean = BluetoothEdrConfigBean(connDvc)
//
//            val piFactory: PIFactory = BluetoothFactory()
//            val printerInterface = piFactory.create()
//
//            printerInterface.configObject = bluetoothEdrConfigBean
//            rtPrinter.printerInterface = printerInterface
//            try {
//                rtPrinter.printerInterface
//                rtPrinter.connect(bluetoothEdrConfigBean)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//
////            MyApplication.bluetoothPort.connect(connDvc)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
    }

    fun print() {
        val escFac: CmdFactory = EscFactory()
        val escCmd = escFac.create()
        escCmd.append(escCmd.headerCmd) //初始化
        escCmd.chartsetName = "UTF-8"

        val commonSetting = CommonSetting()
        commonSetting.lableSizeBean = LableSizeBean(60, 40)
        commonSetting.align = CommonEnum.ALIGN_LEFT
        escCmd.append(escCmd.getCommonSettingCmd(commonSetting))

        val textSetting = TextSetting()
        textSetting.escFontType = ESCFontTypeEnum.FONT_D_8x16
        try {
            val preBlank = ""
            textSetting.align = CommonEnum.ALIGN_LEFT
            escCmd.append(
                escCmd.getTextCmd(
                    textSetting,
                    preBlank + "Order Date : 2024.07.19 10:50"
                )
            )
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "Order No   : A08"))
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.getTextCmd(textSetting, preBlank + "Table No   : 001\n"))
            escCmd.append(escCmd.lfcrCmd)
            textSetting.doubleWidth = SettingEnum.Enable //倍宽
            escCmd.append(escCmd.getTextCmd(textSetting, "Product       Qty  Amt "))
            escCmd.append(escCmd.lfcrCmd)
            textSetting.doubleHeight = SettingEnum.Disable //倍高
            textSetting.doubleWidth = SettingEnum.Disable //倍宽
            escCmd.append(
                escCmd.getTextCmd(
                    textSetting,
                    "$preBlank--------------------------------------------"
                )
            )
            escCmd.append(escCmd.lfcrCmd)
            textSetting.doubleWidth = SettingEnum.Enable //倍宽
            escCmd.append(escCmd.getTextCmd(textSetting, "Rice Noddle   1    9.45 \r"))
            escCmd.append(escCmd.getTextCmd(textSetting, "- Extra Meat"))
            escCmd.append(escCmd.lfcrCmd)
            escCmd.append(escCmd.cmdCutNew) //切刀指令
            rtPrinter.writeMsgAsync(escCmd.appendCmds)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            Log.d(TAG, "Print Error >> $e")
        }
    }

    override fun printerObserverCallback(printerInterface: PrinterInterface<*>, state: Int) {
        Log.i(TAG, "printerObserverCallback:state= $state")
        runOnUiThread {
            when (state) {
                CommonEnum.CONNECT_STATE_SUCCESS -> {
                    loadingDialog.dismiss()
                    Toast.makeText(mActivity, "BluetoothConnect Success", Toast.LENGTH_SHORT).show()

                    curPrinterInterface = printerInterface
//                    printerInterfaceArrayList.add(printerInterface)
                    rtPrinter.setPrinterInterface(printerInterface)
                    //  BaseApplication.getInstance().setRtPrinter(rtPrinter);
//                        setPrinterStatusListener();//StatusListener()
                }

                CommonEnum.CONNECT_STATE_INTERRUPTED -> {
                    if (printerInterface != null && printerInterface.getConfigObject() != null) {
                        Toast.makeText(mActivity, "${printerInterface.getConfigObject()} Disconnect", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(mActivity, "Disconnect", Toast.LENGTH_SHORT).show()
                    }
                    curPrinterInterface = null;
//                    printerInterfaceArrayList.remove(printerInterface);//多连接-从已连接列表中移除
                    //  BaseApplication.getInstance().setRtPrinter(null);
                }

                else -> { }
            }
        }
    }

    override fun printerReadMsgCallback(p0: PrinterInterface<*>?, p1: ByteArray?) {
        Log.i(TAG, "printerReadMsgCallback: " + FuncUtils.ByteArrToHex(p1))
    }
}