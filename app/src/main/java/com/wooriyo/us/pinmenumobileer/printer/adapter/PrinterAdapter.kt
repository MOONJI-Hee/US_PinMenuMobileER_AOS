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
import com.rt.printerlibrary.enumerate.ConnectStateEnum
import com.sewoo.jpos.command.ESCPOSConst
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.common.dialog.AlertDialog
import com.wooriyo.us.pinmenumobileer.config.AppProperties
import com.wooriyo.us.pinmenumobileer.databinding.ListPrinterBinding
import com.wooriyo.us.pinmenumobileer.printer.DetailPrinterActivity
import com.wooriyo.us.pinmenumobileer.printer.SetConnActivity
import com.wooriyo.us.pinmenumobileer.util.PrinterHelper
import java.io.IOException

class PrinterAdapter(val dataSet: ArrayList<BluetoothDevice>): RecyclerView.Adapter<PrinterAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListPrinterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class ViewHolder(val binding: ListPrinterBinding, val context: Context): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: BluetoothDevice) {
            Log.d("Sewoo Adapter",  "BluetoothDevice bontState > ${data.bondState}")
            Log.d("Sewoo Adapter",  "BluetoothDevice uuids > ${data.uuids}")
            Log.d("Sewoo Adapter",  "BluetoothDevice bluetoothClass > ${data.bluetoothClass}")
            Log.d("Sewoo Adapter",  "BluetoothDevice address > ${data.address}")
            Log.d("Sewoo Adapter",  "BluetoothDevice name > ${data.name}")
            Log.d("Sewoo Adapter",  "BluetoothDevice alias > ${data.alias}")
            Log.d("Sewoo Adapter",  "BluetoothDevice type > ${data.type}")

            var img = 0
            var model = ""
            val isSewoo = PrinterHelper.checkSewoo(data)

            if (isSewoo) {
                model = "SEWOO SKL-TS400B"
                img = R.drawable.skl_ts400b
            }else {
                model = "RONGTA RP325"
                img = R.drawable.rp325
            }

            binding.ivPrinter.setImageResource(img)
            binding.model.text = model
            binding.address.text = "[${data.address}]"
            binding.nick.text = data.alias.toString()

            // 연결 상태에 따라 우측 버튼 및 뷰 변경
            val connStatus =
                if(isSewoo) {
                    MyApplication.bluetoothPort.isConnected && MyApplication.connDev_sewoo == data.address
                }else {
    //                MyApplication.rtPrinter.getPrinterInterface() != null && MyApplication.rtPrinter.connectState == ConnectStateEnum.Connected && MyApplication.rtPrinter.printerInterface.configObject as BluetoothDevice == data
                    MyApplication.rtPrinter.getPrinterInterface() != null && MyApplication.rtPrinter.connectState == ConnectStateEnum.Connected
                }

            if(connStatus) {
                binding.btnConn.visibility = View.INVISIBLE
                binding.connNo.visibility = View.INVISIBLE
                binding.btnDisConn.visibility = View.VISIBLE
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
                binding.btnDisConn.visibility = View.GONE
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
                (context as SetConnActivity).loadingDialog.show(context.supportFragmentManager)
                (context as SetConnActivity).connPos = adapterPosition
                if(isSewoo)
                    PrinterHelper.connSewoo(context, MyApplication.remoteDevices[adapterPosition])
                else
                    PrinterHelper.connRT(context, MyApplication.remoteDevices[adapterPosition])
            }

            binding.btnDisConn.setOnClickListener {
                // TODO 프린터 연결 해제
            }
        }
    }
}