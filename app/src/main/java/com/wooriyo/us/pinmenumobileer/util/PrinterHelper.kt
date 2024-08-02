package com.wooriyo.us.pinmenumobileer.util

import android.bluetooth.BluetoothDevice
import android.util.Log
import android.widget.Toast
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.connect.PrinterInterface
import com.rt.printerlibrary.enumerate.ConnectStateEnum
import com.rt.printerlibrary.factory.connect.BluetoothFactory
import com.rt.printerlibrary.factory.connect.PIFactory
import com.rt.printerlibrary.printer.ThermalPrinter
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.R

class PrinterHelper {
    companion object {
        fun checkSewoo(bluetoothDevice: BluetoothDevice): Boolean {
            return when(bluetoothDevice.address.substring(0, 8)) {
                "60:6E:41" -> false
                "00:13:7B" -> true
                else -> false
            }
        }

        fun connRT() {
            val configObj = BluetoothEdrConfigBean(MyApplication.remoteDevices[position])
            val bluetoothEdrConfigBean = configObj as BluetoothEdrConfigBean

            val piFactory: PIFactory = BluetoothFactory()
            val printerInterface = piFactory.create() as PrinterInterface

            printerInterface.configObject = bluetoothEdrConfigBean
            MyApplication.rtPrinter.setPrinterInterface(printerInterface)

            try {
                (MyApplication.rtPrinter as ThermalPrinter).connect(bluetoothEdrConfigBean)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("PrinterHelper", "RP325 Connect Error > $e")
                Toast.makeText(mActivity, "Bluetooth Connection Fail", Toast.LENGTH_SHORT).show()
            }
        }

//        fun checkConn () : Boolean {
//            return = if(isSewoo) {
//                MyApplication.bluetoothPort.isConnected && MyApplication.connDev_sewoo == data.address
//            }else {
//                MyApplication.rtPrinter.getPrinterInterface() != null && MyApplication.rtPrinter.connectState == ConnectStateEnum.Connected && MyApplication.rtPrinter.printerInterface.configObject as BluetoothDevice == data
//            }
//        }
    }
}