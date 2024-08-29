package com.wooriyo.us.pinmenumobileer.broadcast

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.sewoo.request.android.RequestHandler
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.MyApplication.Companion.remoteDevices
import com.wooriyo.us.pinmenumobileer.util.AppHelper

class BtDiscoveryReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        var bFlag = true
        var btDev: BluetoothDevice
        val remoteDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        if (remoteDevice != null) {
            val devNum = remoteDevice.bluetoothClass.majorDeviceClass
            if (devNum != BluetoothClass.Device.Major.IMAGING) return

            if (MyApplication.bluetoothPort.isValidAddress(remoteDevice.address)) {
                for (i in remoteDevices.indices) {
                    btDev = remoteDevices.elementAt(i)
                    if (remoteDevice.address == btDev.address) {
                        bFlag = false
                        break
                    }
                }
                if (bFlag) {
                    remoteDevices.add(remoteDevice)
                }
            }
        }else
            Toast.makeText(context, "검색된 블루투스 기기가 없습니다.", Toast.LENGTH_SHORT).show()
    }
}