package com.wooriyo.us.pinmenumobileer.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeidx
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.ActivitySetGoodsBinding
import com.wooriyo.us.pinmenumobileer.menu.adapter.GoodsAdapter
import com.wooriyo.us.pinmenumobileer.model.GoodsDTO
import com.wooriyo.us.pinmenumobileer.model.GoodsListDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SetGoodsActivity : BaseActivity() {
    lateinit var binding: ActivitySetGoodsBinding

    lateinit var goodsList: ArrayList<GoodsDTO>
    lateinit var goodsAdapter: GoodsAdapter

    var cate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetGoodsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.text = intent.getStringExtra("catename") ?: ""

        cate = intent.getStringExtra("catecode") ?: ""

        goodsList = ArrayList<GoodsDTO>()
        goodsAdapter = GoodsAdapter(goodsList, cate)

        binding.rvGoods.layoutManager = GridLayoutManager(mActivity, 2)
        binding.rvGoods.adapter = goodsAdapter

        binding.back.setOnClickListener { finish() }
        binding.changeSeq.setOnClickListener {
            val intent = Intent(mActivity, ChangeMenuSeqActivity::class.java)
            intent.putExtra("goodsList", goodsList)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        getMenu()
    }

    fun getMenu() {
        ApiClient.service.getGoods(useridx, storeidx, cate).enqueue(object : Callback<GoodsListDTO> {
            override fun onResponse(call: Call<GoodsListDTO>, response: Response<GoodsListDTO>) {
                Log.d(TAG, "메뉴 리스트 조회 url : $response")
                if(!response.isSuccessful) return

                val result = response.body() ?: return
                when(result.status){
                    1 -> {
                        goodsList.clear()
                        goodsList.addAll(result.glist)

                        if(goodsList.size > 1) {
                            binding.changeSeq.visibility = View.VISIBLE
                        }else
                            binding.changeSeq.visibility = View.GONE

                        goodsAdapter.notifyDataSetChanged()
                    }
                    else -> Toast.makeText(mActivity, result.msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GoodsListDTO>, t: Throwable) {
                Toast.makeText(mActivity, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "메뉴 리스트 조회 실패 > $t")
            }
        })
    }
}