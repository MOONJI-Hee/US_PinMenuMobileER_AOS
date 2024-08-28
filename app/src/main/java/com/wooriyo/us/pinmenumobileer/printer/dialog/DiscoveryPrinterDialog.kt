package com.wooriyo.us.pinmenumobileer.printer.dialog

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.wooriyo.us.pinmenumobileer.BaseDialogFragment
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.databinding.DialogDiscoveryPrinterBinding
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.printer.SetConnActivity
import com.wooriyo.us.pinmenumobileer.printer.adapter.DiscoveryPrinterAdapter

class DiscoveryPrinterDialog(val dataSet: ArrayList<BluetoothDevice>): BaseDialogFragment() {
    lateinit var binding: DialogDiscoveryPrinterBinding
    val TAG = "DiscoveryPrinterDialog"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogDiscoveryPrinterBinding.inflate(layoutInflater)

        binding.rvPrinter.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvPrinter.adapter = DiscoveryPrinterAdapter(dataSet).apply {
            setOnItemClickListener(object : ItemClickListener {
                override fun onItemClick(position: Int) {
                    MyApplication.remoteDevices.add(dataSet[position])
                    (context as SetConnActivity).printerAdapter.notifyItemInserted(MyApplication.remoteDevices.lastIndex)
                    dismiss()
                }
            })
        }

        binding.cancel.setOnClickListener { dismiss() }

        return binding.root
    }
}