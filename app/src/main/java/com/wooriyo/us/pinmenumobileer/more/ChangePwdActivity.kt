package com.wooriyo.us.pinmenumobileer.more

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.pref
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivityChangePwdBinding
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePwdActivity : BaseActivity() {
    lateinit var binding: ActivityChangePwdBinding

//    val mActivity = this@ChangePwdActivity
//    val TAG = "ChangePwdActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePwdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 비밀번호 노출하는 거 맞는지 다시 확인
        binding.nowPwd.setText(pref.getPw())

        binding.back.setOnClickListener { finish() }
        binding.save.setOnClickListener {
            val nowPw = binding.nowPwd.text.toString()
            val newPw = binding.newPwd.text.toString()

            if(nowPw.isEmpty())
                Toast.makeText(this@ChangePwdActivity, R.string.msg_no_now_pw, Toast.LENGTH_SHORT).show()
            else if(newPw.isEmpty())
                Toast.makeText(this@ChangePwdActivity, R.string.msg_no_new_pw, Toast.LENGTH_SHORT).show()
            else {
                ApiClient.service.changePwd(MyApplication.useridx, nowPw, newPw).enqueue(object : Callback<ResultDTO>{
                    override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                        Log.d(TAG, "비밀번호 변경 url : $response")
                        if(!response.isSuccessful) return

                        val result = response.body() ?: return
                        when(result.status) {
                            1 -> {
                                Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
                                MyApplication.pref.setPw(newPw)
                                finish()
                            }
                            else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                        Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "비밀번호 변경 실패 >> $t")
                        Log.d(TAG, "비밀번호 변경 >> ${call.request()}")
                    }
                })
            }
        }
    }
}