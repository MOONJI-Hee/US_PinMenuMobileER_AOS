package com.wooriyo.us.pinmenumobileer.qr.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wooriyo.us.pinmenumobileer.BaseDialogFragment
import com.wooriyo.us.pinmenumobileer.databinding.DialogQrInfoBinding

class QrInfoDialog: BaseDialogFragment() {
    lateinit var binding: DialogQrInfoBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogQrInfoBinding.inflate(layoutInflater)

        binding.confirm.setOnClickListener { dismiss() }

        return binding.root
    }
}