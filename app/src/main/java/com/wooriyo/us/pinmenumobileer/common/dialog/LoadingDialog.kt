package com.wooriyo.us.pinmenumobileer.common.dialog

import android.app.ActionBar.LayoutParams
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.wooriyo.us.pinmenumobileer.databinding.DialogLoadingBinding
import com.wooriyo.us.pinmenumobileer.BaseDialogFragment

class LoadingDialog: BaseDialogFragment() {
    lateinit var binding: DialogLoadingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogLoadingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val window = dialog?.window ?: return
        val params = window.attributes
        params.width = LayoutParams.MATCH_PARENT
        params.height = LayoutParams.MATCH_PARENT
        window.attributes = params
    }

    fun show(manager: FragmentManager) {
        if(!isAdded) {
            show(manager, "LoadingDialog")
        }
    }
}