package com.wooriyo.us.pinmenumobileer.printer

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.androidId
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeidx
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivitySupportPrinterBinding
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectPrinterActivity : BaseActivity() {
    lateinit var binding: ActivitySupportPrinterBinding

//    val TAG = "SelectPrinterActivity"
//    val mActivity = this@SelectPrinterActivity

    var type = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportPrinterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.run {
            // SupportPrinterActivity와 Layout 같이 쓰기 때문에 뷰 변경해주기
            title.text = getString(R.string.printer_title_sel_model)
            ckRp325.visibility = View.VISIBLE
            select.visibility = View.VISIBLE
            supportInfo.visibility = View.GONE
            openRp325.visibility = View.GONE

            rp325.setOnClickListener {
                ckRp325.isChecked = true
            }

//            ckRp325.setOnCheckedChangeListener { _, isChecked ->
//                if(isChecked) {
//                }
//            }

            select.setOnClickListener {
                when {
                    ckRp325.isChecked -> type = 1
                }
                setPrinterModel()
            }

            back.setOnClickListener { finish() }
        }
    }

    fun setPrinterModel() {
        ApiClient.service.udtPrintModel(useridx, storeidx, androidId, type, "N").enqueue(object : Callback<ResultDTO>{
            override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                Log.d(TAG, "프린터 모델 선택 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return
                when(result.status) {
                    1 -> {
                        intent.putExtra("printType", type)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                    else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "프린터 모델 선택 오류 >> $t")
                Log.d(TAG, "프린터 모델 선택 오류 >> ${call.request()}")
            }
        })
    }
}