package com.wooriyo.us.pinmenumobileer.util

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean
import com.rt.printerlibrary.connect.PrinterInterface
import com.rt.printerlibrary.factory.connect.BluetoothFactory
import com.rt.printerlibrary.factory.connect.PIFactory
import com.rt.printerlibrary.printer.ThermalPrinter
import com.sewoo.request.android.RequestHandler
import com.wooriyo.us.pinmenumobileer.MyApplication
import java.io.IOException

class PrinterHelper {
    companion object {
        fun checkSewoo(bluetoothDevice: BluetoothDevice): Boolean {
            return when(bluetoothDevice.address.substring(0, 8)) {
                "60:6E:41" -> false
                "00:13:7B" -> true
                else -> false
            }
        }

        fun connRT(context: Context, bluetoothDevice: BluetoothDevice) {
            if(MyApplication.remoteDevices.isEmpty()) {
                Toast.makeText(context, "There is no connectable Bluetooth device.", Toast.LENGTH_SHORT).show()
                return
            }

            val configObj = BluetoothEdrConfigBean(bluetoothDevice)
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
                Toast.makeText(context, "Bluetooth Connection Fail", Toast.LENGTH_SHORT).show()
            }
        }

        fun connSewoo(context: Context, bluetoothDevice: BluetoothDevice) {
            Log.d("PrinterHelper", "세우테크 프린터 커넥트 시작")

            if(MyApplication.remoteDevices.isEmpty()) {
                Toast.makeText(context, "There is no connectable Bluetooth device.", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                MyApplication.bluetoothPort.connect(bluetoothDevice)
                MyApplication.connDev_sewoo = bluetoothDevice.address

                val rh = RequestHandler()
                MyApplication.btThread = Thread(rh)
                MyApplication.btThread!!.start()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("PrinterHelper", "Sewoo Connect Error > $e")
                Toast.makeText(context, "Bluetooth Connection Fail", Toast.LENGTH_SHORT).show()
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