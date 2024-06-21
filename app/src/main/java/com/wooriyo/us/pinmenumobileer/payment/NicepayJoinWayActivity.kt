package com.wooriyo.us.pinmenumobileer.payment

import android.os.Bundle
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.databinding.ActivityNicepayJoinWayBinding

class NicepayJoinWayActivity : BaseActivity() {
    lateinit var binding: ActivityNicepayJoinWayBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNicepayJoinWayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener { finish() }
    }
}