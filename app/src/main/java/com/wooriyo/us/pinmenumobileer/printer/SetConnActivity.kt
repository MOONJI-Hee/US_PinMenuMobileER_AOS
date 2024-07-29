package com.wooriyo.us.pinmenumobileer.printer

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
import com.sewoo.request.android.RequestHandler
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.androidId
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.bluetoothAdapter
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.remoteDevices
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeidx
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.config.AppProperties
import com.wooriyo.us.pinmenumobileer.databinding.ActivitySetConnBinding
import com.wooriyo.us.pinmenumobileer.listener.DialogListener
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.model.PrintContentDTO
import com.wooriyo.us.pinmenumobileer.printer.adapter.PrinterAdapter
import com.wooriyo.us.pinmenumobileer.printer.dialog.SetNickDialog
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import com.wooriyo.us.pinmenumobileer.util.AppHelper
import com.wooriyo.us.pinmenumobileer.util.AppHelper.Companion.connDevice
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SetConnActivity : BaseActivity() {
    lateinit var binding: ActivitySetConnBinding

    val printerAdapter = PrinterAdapter(remoteDevices)

    var connPos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetConnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 디바이스, 프린터 정보 조회
        getPrintSetting()

        // 페어링된 프린터 리스트 조회
//        getPairedDevice()

        // BroadCast Receiver
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null) return

                var bFlag = true
                var btDev: BluetoothDevice
                val remoteDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (remoteDevice != null) {
                    val devNum = remoteDevice.bluetoothClass.majorDeviceClass
                    if (devNum != AppProperties.BT_PRINTER) return

                    if (MyApplication.bluetoothPort.isValidAddress(remoteDevice.address)) {
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
                    val retVal = AppHelper.connDevice(0)

                    if (retVal == 0) { // Connection success.
                        val rh = RequestHandler()
                        MyApplication.btThread = Thread(rh)
                        MyApplication.btThread!!.start()
                    } else // Connection failed.
                        Toast.makeText(context, "블루투스 연결 실패", Toast.LENGTH_SHORT).show()
                }else
                    Toast.makeText(context, "검색된 블루투스 기기가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }, IntentFilter(BluetoothDevice.ACTION_FOUND))
//        registerReceiver(connectDevice, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
//        registerReceiver(connectDevice, IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))

        // 연결 프린트 리사이클러뷰
        printerAdapter.setConnClickListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                var status = ""

                if (remoteDevices[position].bondState == 0) {
                    MyApplication.bluetoothPort.disconnect()

//                    printerList[position].connected = false

                    status = "N"
                } else {
                    val retVal = connDevice(position)

                    if (retVal == 0) {
                        val rh = RequestHandler()
                        MyApplication.btThread = Thread(rh)
                        MyApplication.btThread!!.start()

//                        printerList[position].connected = true

                        status = "Y"

                        val prePos = connPos
                        connPos = position
                        printerAdapter.notifyItemChanged(position)

//                        if (prePos != connPos)
//                            printerAdapter.notifyItemChanged(prePos)

                    }else if (retVal == -2) {
                        AppHelper.searchDevice()
                    } else {
                        Toast.makeText(mActivity, "블루투스 연결 실패", Toast.LENGTH_SHORT).show()
                        status = "N"
                    }
                }
                val prePos = connPos
                connPos = position
                printerAdapter.notifyItemChanged(position)

                if (prePos != connPos)
                    printerAdapter.notifyItemChanged(prePos)

//                setPrintConnStatus(position, status)
            }
        })

//        binding.rvPrinter.layoutManager = LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
//        binding.rvPrinter.adapter = printerAdapter

        binding.rvSewoo.layoutManager = LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
        binding.rvSewoo.adapter = printerAdapter

        val nickDialog = SetNickDialog("", 1, "안드로이드 스마트폰")
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
//            startActivity(Intent(mActivity, NewConnActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
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

    fun setPrintConnStatus(position: Int, status: String) {
//        ApiClient.service.setPrintConnStatus(useridx, storeidx, androidId, printerList[position].idx, status)
//            .enqueue(object : Callback<ResultDTO>{
//                override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
//                    Log.d(TAG, "연결 프린터 상태 갱신 Url : $response")
//                    if(!response.isSuccessful) return
//
//                    val result = response.body() ?: return
//
//                    when(result.status) {
//                        1 -> {}
//                        else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
//                    Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
//                    Log.d(TAG, "연결 프린터 상태 갱신 오류 >> $t")
//                    Log.d(TAG, "연결 프린터 상태 갱신 오류 >> ${call.request()}")
//                }
//            })
    }

//    fun getConnPrintList() {
//        ApiClient.service.connPrintList(useridx, storeidx, androidId).enqueue(object : Callback<PrintListDTO> {
//            override fun onResponse(call: Call<PrintListDTO>, response: Response<PrintListDTO>) {
//                Log.d(TAG, "등록된 프린터 리스트 조회 URL : $response")
//                if(!response.isSuccessful) return
//
//                val result = response.body() ?: return
//
//                when(result.status) {
//                    1 -> {
//                        printerList.clear()
//                        printerList.addAll(result.myprintList)
//                        printerAdapter.notifyDataSetChanged()
//
//                        checkConn()
//                    }
//                    else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<PrintListDTO>, t: Throwable) {
//                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
//                Log.d(TAG, "등록된 프린터 리스트 조회 오류 >> $t")
//                Log.d(TAG, "등록된 프린터 리스트 조회 오류 >> ${call.request()}")
//            }
//        })
//    }
}