package com.wooriyo.us.pinmenumobileer.more

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeidx
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivitySetUseLangBinding
import com.wooriyo.us.pinmenumobileer.model.LanguageDTO
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.more.adapter.LanguageAdapter
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SetUseLangActivity: BaseActivity() {
    lateinit var binding: ActivitySetUseLangBinding
    lateinit var lang: String
    lateinit var arrLangCode : Array<String>

    val languageList = ArrayList<LanguageDTO>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetUseLangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lang = intent.getStringExtra("language") ?: ""

        setLanguageList()

        binding.back.setOnClickListener { finish() }
        binding.save.setOnClickListener { setLanguage() }
    }

    fun setLanguageList() {
        val jsonObject = JSONObject(lang)
        val arrLang = resources.getStringArray(R.array.language)
        val arrImg = resources.getStringArray(R.array.language_img)
        arrLangCode = resources.getStringArray(R.array.language_code)

        arrLang.forEachIndexed { i, it ->
            languageList.add(LanguageDTO(it, arrImg[i], jsonObject.get(arrLangCode[i]) == "Y"))
        }

        binding.rv.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        binding.rv.adapter = LanguageAdapter(languageList)
    }

    fun setLanguage() {
        val lang = JSONObject()

        languageList.forEachIndexed { i, it ->
            val buse = if(it.isChecked) "Y" else "N"
            lang.put(arrLangCode[i], buse)
        }

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