package com.wooriyo.us.pinmenumobileer.printer.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.sewoo.jpos.command.ESCPOSConst
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.common.dialog.AlertDialog
import com.wooriyo.us.pinmenumobileer.config.AppProperties
import com.wooriyo.us.pinmenumobileer.databinding.ListPrinterBinding
import com.wooriyo.us.pinmenumobileer.listener.ItemClickListener
import com.wooriyo.us.pinmenumobileer.printer.DetailPrinterActivity
import java.io.IOException

class PrinterAdapter(val dataSet: ArrayList<BluetoothDevice>): RecyclerView.Adapter<PrinterAdapter.ViewHolder>() {
    lateinit var itemClickListener: ItemClickListener

    fun setConnClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListPrinterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, parent.context, itemClickListener)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    class ViewHolder(val binding: ListPrinterBinding, val context: Context, val itemClickListener: ItemClickListener): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: BluetoothDevice) {
            Log.d("Sewoo Adapter",  "BluetoothDevice bontState > ${data.bondState}")
            Log.d("Sewoo Adapter",  "BluetoothDevice uuids > ${data.uuids}")
            Log.d("Sewoo Adapter",  "BluetoothDevice bluetoothClass > ${data.bluetoothClass}")
            Log.d("Sewoo Adapter",  "BluetoothDevice address > ${data.address}")
            Log.d("Sewoo Adapter",  "BluetoothDevice name > ${data.name}")
            Log.d("Sewoo Adapter",  "BluetoothDevice alias > ${data.alias}")
            Log.d("Sewoo Adapter",  "BluetoothDevice type > ${data.type}")

//            BluetoothDevice bontState > 12
//            BluetoothDevice uuids > [Landroid.os.ParcelUuid;@1f48c94
//            BluetoothDevice bluetoothClass > 680
//            BluetoothDevice address > 00:13:7B:40:01:25
//            BluetoothDevice name > POS Printer
//            BluetoothDevice alias > POS Printer
//            BluetoothDevice type > 1

            var img = 0
            var model = ""

            when(data.address.substring(0, 8)) {
                "60:6E:41" -> {
                    model = "RONGTA RP325"
                    img = R.drawable.rp325
                }
                "00:13:7B" -> {
                    model = "SEWOO SKL-TS400B"
                    img = R.drawable.skl_ts400b
                }
            }

            binding.ivPrinter.setImageResource(img)
            binding.model.text = "$model\n[${data.address}]"
            binding.nick.text = data.alias.toString()

            // 연결 상태에 따라 우측 버튼 및 뷰 변경
            if(MyApplication.bluetoothPort.isConnected && MyApplication.connDev_sewoo == data.address) {
                binding.btnConn.visibility = View.INVISIBLE
                binding.connNo.visibility = View.INVISIBLE
                binding.btnClear.visibility = View.VISIBLE
                binding.connDot.visibility = View.VISIBLE
                binding.connStatus.visibility = View.VISIBLE
                binding.connStatus.text = context.getString(R.string.good)

                binding.btnTest.setOnClickListener {
                    try {
                        MyApplication.escposPrinter.printAndroidFont(context.getString(R.string.print_test),
                            AppProperties.FONT_WIDTH,
                            AppProperties.FONT_SMALL, ESCPOSConst.LK_ALIGNMENT_LEFT)
                        MyApplication.escposPrinter.printAndroidFont(context.getString(R.string.print_test),
                            AppProperties.FONT_WIDTH,
                            AppProperties.FONT_BIG, ESCPOSConst.LK_ALIGNMENT_LEFT)
                        MyApplication.escposPrinter.lineFeed(4)
                        MyApplication.escposPrinter.cutPaper()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else {
                binding.btnConn.visibility = View.VISIBLE
                binding.connNo.visibility = View.VISIBLE
                binding.btnClear.visibility = View.GONE
                binding.connDot.visibility = View.GONE
                binding.connStatus.visibility = View.GONE

                binding.btnTest.setOnClickListener {
                    val fragmentActivity = context as FragmentActivity
                    AlertDialog("", context.getString(R.string.dialog_no_printer)).show(fragmentActivity.supportFragmentManager, "AlertDialog")
                }
            }

            binding.layout.setOnClickListener {
                val intent = Intent(context, DetailPrinterActivity::class.java)
                intent.putExtra("device", data)
                intent.putExtra("model", model)
                intent.putExtra("img", img)
                context.startActivity(intent)
            }

            binding.btnConn.setOnClickListener {
                itemClickListener.onItemClick(adapterPosition)
            }

            binding.btnClear.setOnClickListener {
                // TODO 프린터 연결 해제
            }
        }
    }
}