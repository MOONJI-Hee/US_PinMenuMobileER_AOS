package com.wooriyo.us.pinmenumobileer.printer.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wooriyo.us.pinmenumobileer.databinding.ListDiscoveryPrinterBinding
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.util.PrinterHelper

class DiscoveryPrinterAdapter(val dataSet: ArrayList<BluetoothDevice>): RecyclerView.Adapter<DiscoveryPrinterAdapter.ViewHolder>() {
    lateinit var itemClickListener: ItemClickListener

    fun setOnItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListDiscoveryPrinterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, itemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class ViewHolder(val binding: ListDiscoveryPrinterBinding, val itemClickListener: ItemClickListener): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: BluetoothDevice) {
            val isSewoo = PrinterHelper.checkSewoo(data)

            binding.run {
                company.text = if (isSewoo) "SEWOO" else "RONGTA"
                model.text = data.name
                address.text = "[${data.address}]"

                layout.setOnClickListener{
                    itemClickListener.onItemClick(adapterPosition)
                }
            }
        }
    }
}