package com.wooriyo.us.pinmenumobileer.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeidx
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivitySetCategoryBinding
import com.wooriyo.us.pinmenumobileer.menu.adapter.CateAdapter
import com.wooriyo.us.pinmenumobileer.model.CateListDTO
import com.wooriyo.us.pinmenumobileer.model.CategoryDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SetCategoryActivity : BaseActivity() {
    lateinit var binding: ActivitySetCategoryBinding

    val cateList = ArrayList<CategoryDTO>()
    val cateAdapter = CateAdapter(cateList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvCate.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        binding.rvCate.adapter = cateAdapter

        binding.back.setOnClickListener { finish() }
        binding.changeSeq.setOnClickListener {
            val intent = Intent(mActivity, ChangeCateSeqActivity::class.java)
            intent.putExtra("cateList", cateList)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        getCategory()
    }

    fun getCategory() {
        ApiClient.service.getCateList(useridx, storeidx)
            .enqueue(object: Callback<CateListDTO> {
                override fun onResponse(call: Call<CateListDTO>, response: Response<CateListDTO>) {
                    Log.d(TAG, "카테고리 조회 url : $response")
                    if(!response.isSuccessful) return

                    val result = response.body() ?: return

                    when(result.status) {
                        1 -> {
                            cateList.clear()
                            cateList.addAll(result.cateList)

                            if(cateList.size > 0) {
                                binding.changeSeq.visibility = View.VISIBLE
                                cateAdapter.notifyDataSetChanged()
                            }else
                                binding.changeSeq.visibility = View.GONE
                        }
                        else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<CateListDTO>, t: Throwable) {
                    Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "카테고리 조회 실패 > $t")
                    Log.d(TAG, "카테고리 조회 실패 > ${call.request()}")
                }
            })
    }
}