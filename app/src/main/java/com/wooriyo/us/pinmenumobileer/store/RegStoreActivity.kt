package com.wooriyo.us.pinmenumobileer.store

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MainActivity
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.setStoreDTO
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.store
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivityRegStoreBinding
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegStoreActivity : BaseActivity() {
    lateinit var binding: ActivityRegStoreBinding
//    val mActivity = this@RegStoreActivity
//    val TAG = "RegStoreActivity"

    var pre = ""

    val setAddr = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK) {
            store.address = it.data?.getStringExtra("address").toString()
            store.long = it.data?.getStringExtra("long").toString()
            store.lat = it.data?.getStringExtra("lat").toString()

            Log.d(TAG, "매장 주소 >> ${store.address}")
            Log.d(TAG, "매장 경도 >> ${store.long}")
            Log.d(TAG, "매장 위도 >> ${store.lat}")

            if(store.address.isNotEmpty())
                binding.etAddr.setText(store.address)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pre = intent.getStringExtra("pre").toString()
        setStoreDTO()

        binding.save.setOnClickListener{ save() }

        Log.d(TAG, "store 생성 useridx >> $useridx")
    }

    fun save() {
        store.name = binding.etName.text.toString()
        store.subname = binding.etName2.text.toString()
        store.address = binding.etAddr.text.toString()

        if(store.name.isEmpty()) {
            Toast.makeText(mActivity, R.string.store_name_hint, Toast.LENGTH_SHORT).show()
        }
//        else if (store.address.isEmpty()) {
//            Toast.makeText(mActivity, R.string.msg_no_addr, Toast.LENGTH_SHORT).show()
//        }
        else {
            ApiClient.service.regStore(useridx, store.name, store.subname, store.address, store.long, store.lat)
                .enqueue(object : Callback<ResultDTO> {
                    override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                        Log.d(TAG, "매장 등록 url : $response")
                        val resultDTO = response.body()
                        if(resultDTO != null) {
                            if(resultDTO.status == 1) {
                                Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
                                if(pre == "signUp")
                                    startActivity(Intent(mActivity, MainActivity::class.java))
                                else finish()
                            }else {
                                Toast.makeText(mActivity, resultDTO.msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                        Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "매장 등록 실패 > $t")
                        Log.d(TAG, "매장 등록 실패 > ${call.request()}")
                    }
                })
        }
    }
}