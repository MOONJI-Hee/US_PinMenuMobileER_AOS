package com.wooriyo.us.pinmenumobileer.payment

import android.content.Intent
import android.os.Bundle
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.databinding.ActivityNicepayInfoBinding

class NicepayInfoActivity : BaseActivity() {
    lateinit var binding: ActivityNicepayInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNicepayInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fromOrder = intent.getStringExtra("fromOrder") ?: ""

        binding.back.setOnClickListener { finish() }
        binding.joinWay.setOnClickListener { startActivity(Intent(this@NicepayInfoActivity, NicepayJoinWayActivity::class.java)) }
        binding.setting.setOnClickListener {
            // 수정 필요
            val intent = Intent(this@NicepayInfoActivity, SetPgInfoActivity::class.java)
            intent.putExtra("fromOrder", fromOrder)
            startActivity(intent)
        }
    }
}