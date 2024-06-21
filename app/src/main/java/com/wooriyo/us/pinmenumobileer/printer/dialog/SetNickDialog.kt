package com.wooriyo.us.pinmenumobileer.printer.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.wooriyo.us.pinmenumobileer.BaseDialogFragment
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.androidId
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.useridx
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.databinding.DialogPrinterNickBinding
import com.wooriyo.us.pinmenumobileer.listener.DialogListener
import com.wooriyo.us.pinmenumobileer.model.ResultDTO
import com.wooriyo.us.pinmenumobileer.util.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SetNickDialog(var nick: String, val type: Int, val model: String): BaseDialogFragment() {
    lateinit var binding: DialogPrinterNickBinding
    lateinit var dialogListener: DialogListener

    val TAG = "SetNickDialog"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogPrinterNickBinding.inflate(layoutInflater)

        if(nick != "")
            binding.etNick.setText(nick)

        binding.model.text = model

        binding.save.setOnClickListener { save() }

        return binding.root
    }

    fun save() {
        nick = binding.etNick.text.toString()
        ApiClient.service.setPrintNick(useridx, MyApplication.storeidx, androidId, nick, type)
            .enqueue(object : Callback<ResultDTO>{
                override fun onResponse(call: Call<ResultDTO>, response: Response<ResultDTO>) {
                    Log.d(TAG, "디바이스 별명 설정 URL >> $response")
                    if (!response.isSuccessful) return

                    val result = response.body() ?: return
                    when(result.status) {
                        1 -> {
                            dialogListener.onNickSet(nick)
                            dismiss()
                        }
                        else -> Toast.makeText(context, result.msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResultDTO>, t: Throwable) {
                    Toast.makeText(context, R.string.msg_retry, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "디바이스 별명 설정 오류 >> $t")
                    Log.d(TAG, "디바이스 별명 설정 오류 >> ${call.request()}")
                }
            })
    }

    fun setOnNickChangeListener(dialogListener: DialogListener) {
        this.dialogListener = dialogListener
    }
}