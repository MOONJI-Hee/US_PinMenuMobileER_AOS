package com.wooriyo.us.pinmenumobileer.printer

import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.androidId
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.bidx
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeidx
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivityDetailPrinterBinding
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailPrinterActivity : BaseActivity() {
    lateinit var binding: ActivityDetailPrinterBinding
    lateinit var printer : BluetoothDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPrinterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        printer =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.getParcelableExtra("device", BluetoothDevice::class.java) ?: return
            else
                intent.getParcelableExtra("device") ?: return


        val img = intent.getIntExtra("img", 0)
        val model = intent.getStringExtra("model")

        binding.ivPrinter.setImageResource(img)
        binding.model.text = model

        binding.etNickPrinter.setText(printer.alias)

        binding.back.setOnClickListener { finish() }
        binding.save.setOnClickListener { save() }
        binding.delete.setOnClickListener { delete() }
    }

    fun save() {
        val nick = binding.etNickPrinter.text.toString()

        //TODO BluetoothDevice Alias 바꾸기
//        printer.setAlias(nick)
        Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
        finish()
    }

    fun delete() {
        // 삭제 전 연결 해제
        if (MyApplication.bluetoothPort.isConnected) MyApplication.bluetoothPort.disconnect()

        ApiClient.service.delPrint(useridx, storeidx, androidId, bidx)
            .enqueue(object : Callback<ResultDTO> {
                override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                    Log.d(TAG, "프린터 삭제 URL : $response")
                    if (!response.isSuccessful) return

                    val result = response.body() ?: return
                    when (result.status) {
                        1 -> {
                            Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                    Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "프린터 삭제 오류 >> $t")
                    Log.d(TAG, "프린터 삭제 오류 >> ${call.request()}")
                }
            })
    }
}