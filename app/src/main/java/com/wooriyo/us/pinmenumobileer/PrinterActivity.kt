package com.wooriyo.us.pinmenumobileer

import android.app.Activity
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
import com.wooriyo.us.pinmenumobileer.databinding.ActivityPrinterBinding
import com.wooriyo.us.pinmenumobileer.history.ByHistoryActivity
import com.wooriyo.us.pinmenumobileer.history.adapter.HistoryAdapter
import java.io.IOException

class PrinterActivity : BaseActivity(), OnClickListener {
    lateinit var binding: ActivityPrinterBinding
    lateinit var thread: Thread
    lateinit var pairedDevices : List<BluetoothDevice>

    val foundDevices = ArrayList<BluetoothDevice>()

    var isSearched = false

    var mBluetoothReceiver : BroadcastReceiver ?= null
    var mBluetoothIntentFilter : IntentFilter ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrinterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "111111111111")


        thread = Thread(Runnable{
            Log.d(TAG, "2222222222")

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
//                    mRegistered = false
                                        val list = StringBuffer()
                                        if(foundDevices.isNotEmpty()){
                                            foundDevices.forEach{
                                                list.append("${it.name} ${it.address} ${it.uuids}\n")
                                            }
                                            binding.list.text = list
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

                registerReceiver(object : BroadcastReceiver(){
                    override fun onReceive(context: Context?, intent: Intent?) {
                        loadingDialog.dismiss()
                        Toast.makeText(mActivity, "BluetoothConnect Success", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(mActivity, ByHistoryActivity::class.java))
                    }
                }, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))

                if(foundDevices.isNotEmpty())
                    connDevice(foundDevices[0])
            }
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

        try {
            MyApplication.bluetoothPort.connect(connDvc)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}