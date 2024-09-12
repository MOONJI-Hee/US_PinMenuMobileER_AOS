package com.wooriyo.us.pinmenumobileer.more

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MainActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivityTimezoneBinding
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class TimezoneActivity : BaseActivity() {
    lateinit var binding: ActivityTimezoneBinding

    var selRadioId = 0
    var selTvId = 0
    var pre = ""    // main : 메인화면, signUp : 회원가입, more : 더보기

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimezoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selRadioId = binding.radioEastern.id
        selTvId = binding.tvEastern.id
        pre = intent.getStringExtra("pre") ?: ""

        binding.run {
            if(pre != "more") back.visibility = View.GONE

            val utc = MyApplication.store.timezone
            when(utc) {
                "UTC-5" -> setRadioClickEvent(radioEastern, tvEastern)
                "UTC-6" -> setRadioClickEvent(radioCentral, tvCentral)
                "UTC-7" -> setRadioClickEvent(radioMountain, tvMountain)
                "UTC-8" -> setRadioClickEvent(radioPacific, tvPacific)
                "UTC-9" -> setRadioClickEvent(radioAlaska, tvAlaska)
                "UTC-10" -> setRadioClickEvent(radioHawaii, tvHawaii)
            }

            eastern.setOnClickListener { setRadioClickEvent(radioEastern, tvEastern) }
            central.setOnClickListener { setRadioClickEvent(radioCentral, tvCentral) }
            mountain.setOnClickListener { setRadioClickEvent(radioMountain, tvMountain) }
            pacific.setOnClickListener { setRadioClickEvent(radioPacific, tvPacific) }
            alaska.setOnClickListener { setRadioClickEvent(radioAlaska, tvAlaska) }
            hawaii.setOnClickListener { setRadioClickEvent(radioHawaii, tvHawaii) }
            back.setOnClickListener { finish() }
            save.setOnClickListener { save() }
        }
    }

    fun setRadioClickEvent (v: RadioButton, tv: TextView) {
        if(selRadioId != v.id) {
            findViewById<RadioButton>(selRadioId).isChecked = false
            findViewById<TextView>(selTvId).setTypeface(null, Typeface.NORMAL)
            v.isChecked = true
            tv.setTypeface(null, Typeface.BOLD)
            selRadioId = v.id
            selTvId = tv.id
        }
    }

    fun save() {
        val selText = findViewById<TextView>(selTvId).text
        val matcher = Pattern.compile("[(](.*?)[)]").matcher(selText)
        var utc = ""

        while (matcher.find()) {
            utc = matcher.group(1) ?: ""
        }

        ApiClient.service.setTimezone(MyApplication.useridx, MyApplication.storeidx, utc).enqueue(object :
            Callback<ResultDTO> {
            override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                Log.d(TAG, "타임존 설정 url : $response")
                val result = response.body() ?: return

                if(result.status == 1) {
                    MyApplication.store.timezone = utc
                    if(pre == "signUp")
                        startActivity(Intent(mActivity, MainActivity::class.java))
                    else finish()
                }else
                    Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "타임존 설정 오류 >> $t")
                Log.d(TAG, "타임존 설정 오류 >> ${call.request()}")
            }
        })
    }
}