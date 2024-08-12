package com.wooriyo.us.pinmenumobileer.more

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.CheckBox
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeidx
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivitySetUseLangBinding
import com.wooriyo.us.pinmenumobileer.model.LangDTO
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.more.adapter.LanguageAdapter
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SetUseLangActivity: BaseActivity() {
    lateinit var binding: ActivitySetUseLangBinding
    lateinit var lang: LangDTO
    var init = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetUseLangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lang = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("language", LangDTO::class.java) ?: LangDTO()
        }else {
            intent.getSerializableExtra("language") as LangDTO
        }

        binding.run {
            back.setOnClickListener { finish() }

            rv.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
            rv.adapter = LanguageAdapter(lang)

            setCheckListener(checkEsp, disableEsp)
            setCheckListener(checkKor, disableKor)

            esp.setOnClickListener {
                checkEsp.isChecked = !checkEsp.isChecked
//                store.esp_buse = if(checkEsp.isChecked) "Y" else "N"
            }

            kor.setOnClickListener {
                checkKor.isChecked = !checkKor.isChecked
//                store.kor_buse = if(checkKor.isChecked) "Y" else "N"
            }

//            checkEsp.isChecked = store.esp_buse == "Y"
//            checkKor.isChecked = store.kor_buse == "Y"
//            checkChn.isChecked = store.chn_buse == "Y"
//            checkJpn.isChecked = store.jpn_buse == "Y"
        }
    }

    override fun onResume() {
        super.onResume()
        init = false
    }

    private fun setCheckListener(checkBox: CheckBox, v: View) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if(!init) setLanguage()

            if(isChecked)
                v.visibility = View.GONE
            else
                v.visibility = View.VISIBLE
        }
    }

    fun setLanguage() {
        val lang = StringBuffer()

        lang.append("usa")
        if(binding.checkEsp.isChecked) lang.append("/esp")
        if(binding.checkKor.isChecked) lang.append("/kor")

//        val list = ArrayList<String>()
//
//        if(binding.checkKor.isChecked) list.add("kor")
//        if(binding.checkEng.isChecked) list.add("eng")
//        if(binding.checkEsp.isChecked) list.add("esp")
//        if(binding.checkChn.isChecked) list.add("chn")
//        if(binding.checkJpn.isChecked) list.add("jpn")
//
//        list.forEach {
//            if(lang.toString() == "")
//                lang.append(it)
//            else
//                lang.append("/$it")
//        }

        ApiClient.service.setLanguage(useridx, storeidx, lang.toString()).enqueue(object : Callback<ResultDTO> {
            override fun onResponse(p0: Call<ResultDTO>, p1: Response<ResultDTO>) {
                Log.d(TAG, "언어 사용 여부 설정 url : $p1")
                if(!p1.isSuccessful) return
                val result = p1.body() ?: return

                Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(p0: Call<ResultDTO>, p1: Throwable) {
                Toast.makeText(applicationContext, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "언어 사용 여부 설정 오류 >> $p1")
                Log.d(TAG, "언어 사용 여부 설정 오류 >> ${p0.request()}")
            }
        })
    }
}