package com.wooriyo.us.pinmenumobileer.menu.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.wooriyo.us.pinmenumobileer.BaseDialogFragment
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.DialogDeleteBinding

class DeleteDialog(val type: Int, val strName: String, val onClickListener: OnClickListener): BaseDialogFragment() {
    lateinit var binding: DialogDeleteBinding
    val TAG = "ConfirmDialog"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogDeleteBinding.inflate(layoutInflater)

        var strType = ""
        if(type == 1) {
            strType = "category"
            binding.info.text = getString(R.string.category_delete_info)
        }else if(type == 2) {
            strType = "menu"
            binding.info.text = getString(R.string.menu_delete_info)
        }

        binding.run {
            title.text = String.format(getString(R.string.dialog_delete_title), strType)
            question.text = String.format(getString(R.string.dialog_delete_confirm), strType)
            name.text = String.format(getString(R.string.dialog_delete_name), strName)

            cancel.setOnClickListener { dismiss() }
            delete.setOnClickListener {
                onClickListener.onClick(it)
                dismiss()
            }
        }

        return binding.root
    }
}