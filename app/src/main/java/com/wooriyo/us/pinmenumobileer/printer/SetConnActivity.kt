package com.wooriyo.us.pinmenumobileer.printer

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.connect.PrinterInterface
import com.rt.printerlibrary.enumerate.CommonEnum
import com.rt.printerlibrary.factory.connect.BluetoothFactory
import com.rt.printerlibrary.factory.connect.PIFactory
import com.rt.printerlibrary.observer.PrinterObserver
import com.rt.printerlibrary.observer.PrinterObserverManager
import com.rt.printerlibrary.printer.ThermalPrinter
import com.rt.printerlibrary.utils.FuncUtils
import com.sewoo.request.android.RequestHandler
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.androidId
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.bluetoothAdapter
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.bluetoothPort
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.remoteDevices
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeidx
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivitySetConnBinding
import com.wooriyo.us.pinmenumobileer.listener.DialogListener
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.model.PrintContentDTO
import com.wooriyo.us.pinmenumobileer.printer.adapter.PrinterAdapter
import com.wooriyo.us.pinmenumobileer.printer.dialog.SetNickDialog
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class SetConnActivity : BaseActivity(), PrinterObserver {
    lateinit var binding: ActivitySetConnBinding
    lateinit var printerAdapter : PrinterAdapter

    var connPos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetConnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 디바이스, 프린터 정보 조회
        getPrintSetting()

        // 프린터 RecyclerView Adapter
        printerAdapter = PrinterAdapter(remoteDevices)
        binding.rvPrinter.layoutManager = LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
        binding.rvPrinter.adapter = printerAdapter

        // BroadCast Receiver
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null) return

                Log.d(TAG, "Receiver Broadcast*******")
                loadingDialog.dismiss()

                var bFlag = true
                var btDev: BluetoothDevice
                val remoteDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (remoteDevice != null) {
                    val devNum = remoteDevice.bluetoothClass.majorDeviceClass
                    if (devNum != BluetoothClass.Device.Major.IMAGING) return

                    if (bluetoothPort.isValidAddress(remoteDevice.address)) {
                        for (i in remoteDevices.indices) {
                            btDev = remoteDevices.elementAt(i)
                            if (remoteDevice.address == btDev.address) {
                                bFlag = false
                                break
                            }
                        }
                        if (bFlag) {
                            remoteDevices.add(remoteDevice)
                        }
                    }

                    printerAdapter.notifyDataSetChanged()

                }else
                    Toast.makeText(context, "검색된 블루투스 기기가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }, IntentFilter(BluetoothDevice.ACTION_FOUND))

        val connectDevice = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
                    Toast.makeText(mActivity, "Bluetooth Connection Success", Toast.LENGTH_SHORT).show()

                    MyApplication.pref.setConnectedPrinter(remoteDevices[connPos])
                    printerAdapter.notifyItemChanged(connPos)

                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
                    try {
                        if (bluetoothPort.isConnected) bluetoothPort.disconnect()
                    } catch (e: IOException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    } catch (e: InterruptedException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                    if (MyApplication.btThread != null && MyApplication.btThread!!.isAlive) {
                        MyApplication.btThread!!.interrupt()
                        MyApplication.btThread = null
                    }
                    Toast.makeText(getApplicationContext(), "BlueTooth Disconnect", Toast.LENGTH_SHORT).show();
                }
            }
        }
        registerReceiver(connectDevice, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
        registerReceiver(connectDevice, IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))

        val nickDialog = SetNickDialog(binding.phoneNick.text.toString(), 1, "안드로이드 스마트폰")
        nickDialog.setOnNickChangeListener(object : DialogListener {
            override fun onNickSet(nick: String) {
                binding.phoneNick.text = nick
            }
        })

        binding.back.setOnClickListener { finish() }
        binding.phoneNick.setOnClickListener { nickDialog.show(supportFragmentManager, "SetNickDialog") }
        binding.plus.setOnClickListener {
            loadingDialog.show(supportFragmentManager)
            bluetoothAdapter.startDiscovery()
        }

        PrinterObserverManager.getInstance().add(this)

    }

    override fun printerObserverCallback(printerInterface: PrinterInterface<*>, state: Int) {
        Log.i(TAG, "printerObserverCallback:state= $state")
        runOnUiThread {
            loadingDialog.dismiss()
            when (state) {
                CommonEnum.CONNECT_STATE_SUCCESS -> {
                    Toast.makeText(mActivity, "Bluetooth Connection Success", Toast.LENGTH_SHORT).show()
                    MyApplication.rtPrinter.setPrinterInterface(printerInterface)
                    MyApplication.pref.setConnectedPrinter(printerInterface.configObject as BluetoothDevice)
                    printerAdapter.notifyItemChanged(connPos)
                }

                CommonEnum.CONNECT_STATE_INTERRUPTED -> {
                    if (printerInterface.configObject != null) {
                        Toast.makeText(mActivity, "${printerInterface.getConfigObject()} Disconnect", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(mActivity, "Disconnect", Toast.LENGTH_SHORT).show()
                    }
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

    fun getPrintSetting() {
        ApiClient.service.getPrintContentSet(useridx, storeidx, androidId).enqueue(object : Callback<PrintContentDTO>{
            override fun onResponse(call: Call<PrintContentDTO>, response: Response<PrintContentDTO>) {
                Log.d(TAG, "프린터 출력 내용 조회 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return
                when(result.status) {
                    1 -> {
                        if(result.admnick.isNotEmpty())
                            binding.phoneNick.text = result.admnick
                    }
                    else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PrintContentDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "프린터 출력 내용 조회 오류 >> $t")
                Log.d(TAG, "프린터 출력 내용 조회 오류 >> ${call.request()}")
            }
        })
    }
}