package com.wooriyo.us.pinmenumobileer.printer

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivitySupportPrinterBinding
import com.wooriyo.us.pinmenumobileer.model.PrintModelDTO
import com.wooriyo.us.pinmenumobileer.model.PrintModelListDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SupportPrinterActivity : BaseActivity() {
    lateinit var binding: ActivitySupportPrinterBinding

    val modelList = ArrayList<PrintModelDTO>()

    var opend_ts = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportPrinterBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        getSupportPrinter()

        binding.back.setOnClickListener { finish() }
        binding.rp325.setOnClickListener {
            if(opend_ts) {
                binding.rp325Info.visibility = View.GONE
                binding.openRp325.setImageResource(R.drawable.icon_list_arrow_down)
                opend_ts = false
            }else {
                binding.rp325Info.visibility = View.VISIBLE
                binding.openRp325.setImageResource(R.drawable.icon_list_arrow_up)
                opend_ts = true
            }
        }
    }

    fun getSupportPrinter() {
        ApiClient.service.getSupportList().enqueue(object : Callback<PrintModelListDTO>{
            override fun onResponse(call: Call<PrintModelListDTO>, response: Response<PrintModelListDTO>) {
                Log.d(TAG, "연결 가능 프린터 리스트 조회 URL : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return
                when(result.status) {
                    1 -> {
                        modelList.clear()
                        modelList.addAll(result.printList)
                    }
                    else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PrintModelListDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "연결 가능 프린터 리스트 조회 오류 >> $t")
                Log.d(TAG, "연결 가능 프린터 리스트 조회 오류 >> ${call.request()}")
            }
        })
    }
}