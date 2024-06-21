package com.wooriyo.us.pinmenumobileer.pg.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wooriyo.us.pinmenumobileer.BaseDialogFragment
import com.wooriyo.us.pinmenumobileer.databinding.DialogNoPgInfoBinding

class NoPgInfoDialog: BaseDialogFragment() {
    lateinit var binding: DialogNoPgInfoBinding
    val TAG = "NoPgInfoDialog"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogNoPgInfoBinding.inflate(layoutInflater)

        binding.confirm.setOnClickListener { dismiss() }

        return binding.root
    }
}