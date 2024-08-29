package com.wooriyo.us.pinmenumobileer.member

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.wooriyo.us.pinmenumobileer.model.MemberDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MainActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.androidId
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.appver
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.pref
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.common.dialog.UpdateDialog
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.util.AppHelper
import retrofit2.Call
import retrofit2.Response

class StartActivity : BaseActivity() {
    var id = ""
    var pw = ""
    var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        checkVersion()
    }

    fun getMbrInfo() {
        val memberDTO = pref.getMbrDTO()

        if(memberDTO == null) {
            startActivity(Intent(mActivity, LoginActivity::class.java).also {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } else {
            id = memberDTO.userid
            pw = pref.getPw().toString()
            token = pref.getToken().toString()
            loginWithApi()
        }

        Log.d(TAG, "Android ID >>> $androidId")
    }

    fun loginWithApi()  {
        ApiClient.service.checkMbr(id, pw, token, MyApplication.os, MyApplication.osver, appver, MyApplication.md, androidId)
            .enqueue(object: retrofit2.Callback<MemberDTO>{
                override fun onResponse(call: Call<MemberDTO>, response: Response<MemberDTO>) {
                    Log.d(TAG, "자동 로그인 url : $response")
                    if(!response.isSuccessful) return
                    val memberDTO = response.body()

                    if(memberDTO != null) {
                        if(memberDTO.status == 1 ) {
                            pref.setMbrDTO(memberDTO)
                            pref.setUserIdx(memberDTO.useridx)

                            startActivity(Intent(mActivity, MainActivity::class.java))
                        }else {
                            Toast.makeText(mActivity, memberDTO.msg, Toast.LENGTH_SHORT).show()
                            // status != 1일 때 (아이디 비번 오류, 필수정보 부족 등) 로그인 화면으로 이동
                            startActivity(Intent(mActivity, LoginActivity::class.java).also {
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        }
                    }
                }
                override fun onFailure(call: Call<MemberDTO>, t: Throwable) {
                    Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "자동 로그인 실패 >> $t")
                    // 네트워트 문제로 로그인 실패했을 때 내장 DB와 비교해서 로그인
//                    loginWithDB()
                }
            })
    }

    fun checkVersion() {
        ApiClient.service.checkVersion(1, appver, 1).enqueue(object : retrofit2.Callback<ResultDTO>{
            override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                Log.d(TAG, "버전 확인 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return

                if(result.status == 1) {
                    val curver = result.curver
                    if(AppHelper.compareVer(curver)) {  // 최신버전 이상
                        getMbrInfo()
                    }else { // 최신버전 이하
                        val dialog = UpdateDialog(result.update, result.updateMsg)
                        dialog.setCancelClickListener { getMbrInfo() }
                        dialog.show(supportFragmentManager, "UpdateDialog")
                    }
                }else {
                    Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "버전 확인 status != 1")
                }
            }

            override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                Log.d(TAG, "버전 확인 실패 >> $t")
            }
        })
    }
}