package com.wooriyo.us.pinmenumobileer.payment

import android.os.Bundle
import com.wooriyo.us.pinmenumobileer.BaseActivity
import com.wooriyo.us.pinmenumobileer.databinding.ActivityReaderModelBinding

class ReaderModelActivity : BaseActivity() {
    lateinit var binding: ActivityReaderModelBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderModelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener { finish() }
    }
}