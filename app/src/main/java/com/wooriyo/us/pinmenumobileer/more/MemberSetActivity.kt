package com.wooriyo.us.pinmenumobileer.more

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivitySignupBinding
import com.wooriyo.us.pinmenumobileer.model.MemberDTO
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import com.wooriyo.us.pinmenumobileer.util.AppHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MemberSetActivity: BaseActivity(), View.OnClickListener {
//    val TAG = "MemberSetActivity"
//    val mActivity = this@MemberSetActivity
    lateinit var binding : ActivitySignupBinding

    var memberDTO: MemberDTO? = null
    var userid : String = ""
    var arpayoId : String = ""

    var arpaLinked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        memberDTO = MyApplication.pref.getMbrDTO()
        if(memberDTO != null) {
            userid = memberDTO!!.userid
            arpayoId = memberDTO!!.arpayoid?:""
        }

        binding.title.text = getString(R.string.title_udt_mbr)
        binding.tvId.text = userid
        binding.etPwd.setText(MyApplication.pref.getPw())

        binding.tvId.visibility = View.VISIBLE
        binding.etId.visibility = View.GONE
        binding.btnCheckId.visibility = View.GONE
        binding.checkResult.visibility = View.GONE
        binding.info1.visibility = View.GONE

        binding.save.text = getText(R.string.save)
        binding.clTerms.visibility = View.GONE

        binding.back.setOnClickListener(this)
        binding.save.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onClick(p0: View?) {
        when(p0) {
            binding.back -> finish()
            binding.save -> save()
        }
    }

    private fun save() {
        val pass : String = binding.etPwd.text.toString()

        if(pass.isEmpty() || pass == "") {
            Toast.makeText(mActivity, R.string.msg_no_pw, Toast.LENGTH_SHORT).show()
            return
        }else if(!AppHelper.verifyPw(pass)) {
            Toast.makeText(mActivity, R.string.msg_typemiss_pw, Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.service.udtMbr(useridx, pass, arpayoId)
            .enqueue(object : Callback<ResultDTO> {
                override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                    Log.d(TAG, "회원정보 수정 url : $response")
                    if(response.body()?.status == 1) {
                        MyApplication.pref.setPw(pass)
                        memberDTO?.arpayoid = arpayoId
                        memberDTO?.let { MyApplication.pref.setMbrDTO(it) }
                        Toast.makeText(this@MemberSetActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()
                        finish()
                    } else
                        Toast.makeText(this@MemberSetActivity, response.body()?.msg, Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                    Toast.makeText(this@MemberSetActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "회원정보 수정 오류 > $t")
                }
            })
    }
}