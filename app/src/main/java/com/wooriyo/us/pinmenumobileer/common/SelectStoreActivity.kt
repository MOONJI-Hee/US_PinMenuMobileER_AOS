package com.wooriyo.us.pinmenumobileer.common

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.storeList
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.common.adapter.StoreAdapter
import com.wooriyo.us.pinmenumobileer.common.dialog.AlertDialog
import com.wooriyo.us.pinmenumobileer.databinding.ActivitySelectStoreBinding
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.more.SetCustomerInfoActivity
import com.wooriyo.us.pinmenumobileer.more.SetMenuUiActivity
import com.wooriyo.us.pinmenumobileer.more.SetStoreImgActivity
import com.wooriyo.us.pinmenumobileer.more.SetUseLangActivity
import com.wooriyo.us.pinmenumobileer.more.TimezoneActivity
import com.wooriyo.us.pinmenumobileer.more.TipTaxActivity
import com.wooriyo.us.pinmenumobileer.util.AppHelper

class SelectStoreActivity : BaseActivity() {
    lateinit var binding: ActivitySelectStoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = intent.getStringExtra("type")

        val storeAdapter = StoreAdapter(storeList)

        storeAdapter.setOnItemClickListener(object : ItemClickListener {
            override fun onItemClick(position: Int) {
                super.onItemClick(position)
                when(type) {
                    "customer_info" -> {
                        if(storeList[position].paytype == 2) {
                            MyApplication.store = storeList[position]
                            MyApplication.storeidx = storeList[position].idx
                            startActivity(Intent(mActivity, SetCustomerInfoActivity::class.java))
                        }else {
                            AlertDialog("", getString(R.string.dialog_no_business)).show(supportFragmentManager, "NoBusinessDialog")
                        }
                    }
                    "viewmode" -> {
                        if(storeList[position].payuse == "Y" && AppHelper.dateNowCompare(storeList[position].endDate)) {
                            MyApplication.store = storeList[position]
                            MyApplication.storeidx = storeList[position].idx
                            startActivity(Intent(mActivity, SetMenuUiActivity::class.java))
                        }else {
                            Toast.makeText(mActivity, R.string.msg_no_pay, Toast.LENGTH_SHORT).show()
                        }
                    }
                    "storeImg" -> {
                        MyApplication.store = storeList[position]
                        MyApplication.storeidx = storeList[position].idx
                        startActivity(Intent(mActivity, SetStoreImgActivity::class.java))
                    }
                    "tiptax" -> {
                        MyApplication.store = storeList[position]
                        MyApplication.storeidx = storeList[position].idx
                        startActivity(Intent(mActivity, TipTaxActivity::class.java))
                    }
                    "language" -> {
                        MyApplication.store = storeList[position]
                        MyApplication.storeidx = storeList[position].idx
                        startActivity(Intent(mActivity, SetUseLangActivity::class.java))
                    }
                    "timezone" -> {
                        MyApplication.store = storeList[position]
                        MyApplication.storeidx = storeList[position].idx
                        startActivity(Intent(mActivity, TimezoneActivity::class.java).apply{ putExtra("pre", "more")})
                    }
                }

            }
        })

        binding.rvStore.layoutManager = LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
        binding.rvStore.adapter = storeAdapter

        binding.back.setOnClickListener { finish() }
    }
}