package com.wooriyo.us.pinmenumobileer.member

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.common.WebViewActivity
import com.wooriyo.us.pinmenumobileer.databinding.ActivitySignupBinding
import com.wooriyo.us.pinmenumobileer.model.MemberDTO
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.store.RegStoreActivity
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import com.wooriyo.us.pinmenumobileer.util.AppHelper
import com.wooriyo.us.pinmenumobileer.util.AppHelper.Companion.verifyEmail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : BaseActivity(), View.OnClickListener {
    lateinit var binding: ActivitySignupBinding
//    val TAG = "SignUpActivity"
//    val mActivity = this@SignupActivity

    var idChecked = false
    var arpaLinked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etId.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if(idChecked) {
                    idChecked = false
                    binding.checkResult.text = ""
                }
            }
        })

        binding.back.setOnClickListener { finish() }
        binding.btnCheckId.setOnClickListener { checkID() }
        binding.save.setOnClickListener { save() }

        // 약관 클릭
        binding.terms1.setOnClickListener(this)
        binding.terms2.setOnClickListener(this)
        binding.terms3.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val termIntent = Intent(mActivity, WebViewActivity::class.java)
        var title = ""
        var url = ""

        when(v) {
            binding.terms1 -> {
                title = "Pinmenu Terms"
                url = "https://pinmenu.net/policy/agreement.php"
            }
            binding.terms2 -> {
                title = "Pinmenu Privacy Policy"
                url = "https://pinmenu.net/policy/app_service.php"
            }
            binding.terms3 -> {
                title = "Pinmenu Marketing"
                url = "https://pinmenu.net/policy/marketing.php"
            }
        }

        termIntent.putExtra("title", title)
        termIntent.putExtra("url", url)

        startActivity(termIntent)
    }

    private fun save() {
        val userid : String = binding.etId.text.toString()
        val pass : String = binding.etPwd.text.toString()
        val token = MyApplication.pref.getToken() ?: ""
        val os = "A"
        val osvs = MyApplication.osver
        val appvs = MyApplication.appver
        val md = MyApplication.md

        if(userid.isEmpty() || userid == "") {
            Toast.makeText(mActivity, R.string.msg_no_id, Toast.LENGTH_SHORT).show()
        }else if (!verifyEmail(userid)) {
            Toast.makeText(mActivity, R.string.msg_typemiss_id, Toast.LENGTH_SHORT).show()
        }else if(!idChecked) {
            Toast.makeText(mActivity, R.string.msg_no_checked, Toast.LENGTH_SHORT).show()
        }else if(pass.isEmpty() || pass == "") {
            Toast.makeText(mActivity, R.string.msg_no_pw, Toast.LENGTH_SHORT).show()
        }else if(!AppHelper.verifyPw(pass)) {
            Toast.makeText(mActivity, R.string.msg_typemiss_pw, Toast.LENGTH_SHORT).show()
        }else if(!binding.check1.isChecked) {
            Toast.makeText(mActivity, R.string.msg_agree_term1, Toast.LENGTH_SHORT).show()
        }else if(!binding.check2.isChecked) {
            Toast.makeText(mActivity, R.string.msg_agree_term2, Toast.LENGTH_SHORT).show()
        }else {
            ApiClient.service.regMember(userid, "", pass, token, os, osvs, appvs, md)
                .enqueue(object : Callback<ResultDTO> {
                    override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                        Log.d(TAG, "회원가입 url : $response")
                        if(response.isSuccessful) {
                            val result = response.body() ?: return
                            if(result.status == 1) {
                                Toast.makeText(mActivity, R.string.msg_complete, Toast.LENGTH_SHORT).show()

                                val useridx = result.useridx
                                val memberDTO = MemberDTO(result.status, result.msg, useridx, userid)

                                MyApplication.pref.setMbrDTO(memberDTO)
                                MyApplication.pref.setUserIdx(memberDTO.useridx)
                                MyApplication.pref.setPw(pass)
                                MyApplication.useridx = useridx

                                val intent = Intent(mActivity, RegStoreActivity::class.java)
                                intent.putExtra("pre", "signUp")
                                startActivity(intent)
                            }else {
                                Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                        Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "회원가입 오류 > $t")
                    }
                })
        }
    }

    // 아이디 중복체크
    fun checkID() {
        val userid = binding.etId.text.toString()

        if(userid.isEmpty() || userid == "") {
            Toast.makeText(mActivity, R.string.msg_no_id, Toast.LENGTH_SHORT).show()
            return
        }else if (!verifyEmail(userid)) {
            Toast.makeText(mActivity, R.string.msg_typemiss_id, Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.service.checkId(userid)
            .enqueue(object : Callback<ResultDTO> {
                override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                    Log.d(TAG, "아이디 중복체크 url : $response")
                    if (!response.isSuccessful) return

                    val result = response.body()
                    if(result != null) {
                        if (result.status == 1) {
                            binding.checkResult.text = getString(R.string.able)
                            binding.checkResult.setTextColor(Color.parseColor("#FF6200"))
                            idChecked = true
                        } else {
                            Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                            binding.checkResult.text = getString(R.string.unable)
                            binding.checkResult.setTextColor(Color.parseColor("#5A5A5A"))
                            idChecked = false
                        }
                    }
                }
                override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                    Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "아이디 중복체크 실패 > $t")
                    Log.d(TAG, "아이디 중복체크 실패 > ${call.request()}")
                }
            })
    }
}