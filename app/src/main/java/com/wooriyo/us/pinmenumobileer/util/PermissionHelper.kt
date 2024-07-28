package com.wooriyo.us.pinmenumobileer.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.wooriyo.us.pinmenumobileer.MyApplication
import com.wooriyo.us.pinmenumobileer.R
import com.wooriyo.us.pinmenumobileer.config.AppProperties

class PermissionHelper(val activity: Activity) {
    val TAG = "PermissionHelper"

    private val pmsLocation = arrayOf (
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @RequiresApi(Build.VERSION_CODES.S)
    private val pmsBluetooth = arrayOf (
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )
//
//    // 위치 권한 확인
//    fun checkPmsLocation() {
//        val deniedPms = ArrayList<String>()
//
//        for (pms in pmsLocation) {
//            Log.d(TAG, "pms > $pms")
//            if(ActivityCompat.checkSelfPermission(activity, pms) == PackageManager.PERMISSION_GRANTED) return
//
//            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, pms)) {
//                AlertDialog.Builder(activity)
//                    .setTitle(R.string.pms_location_title)
//                    .setMessage(R.string.pms_location_content)
//                    .setPositiveButton(R.string.confirm) { dialog, _ ->
//                        dialog.dismiss()
//                        getLocationPms()
//                    }
//                    .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss()}
//                    .show()
//                return
//            }else {
//                deniedPms.add(pms)
//            }
//        }
//
//        if(deniedPms.isEmpty()) {
//            if (MyApplication.osver >= Build.VERSION_CODES.S)
//                checkBluetoothPermission()
//            else
//                checkBluetooth()
//        }else {
//            getLocationPms()
//        }
//    }
//
//    fun checkPmsBluetooth() {
//        val deniedPms = ArrayList<String>()
//
//        for (pms in permissionsBt) {
//            if(ActivityCompat.checkSelfPermission(mActivity, pms) != PackageManager.PERMISSION_GRANTED) {
//                if(ActivityCompat.shouldShowRequestPermissionRationale(mActivity, pms)) {
//                    AlertDialog.Builder(mActivity)
//                        .setTitle(R.string.pms_bluetooth_title)
//                        .setMessage(R.string.pms_bluetooth_content)
//                        .setPositiveButton(R.string.confirm) { dialog, _ ->
//                            getBluetoothPms()
//                            dialog.dismiss()
//                            return@setPositiveButton
//                        }
//                        .setNegativeButton(R.string.cancel) { dialog, _ ->
//                            dialog.dismiss()
//                            return@setNegativeButton
//                        }
//                        .show()
//                    return
//                }else
//                    deniedPms.add(pms)
//            }
//        }
//
//        if(deniedPms.isEmpty() || deniedPms.size == 0) {
//            checkBluetooth()
//        }else {
//            getBluetoothPms()
//        }
//    }
//
//    //권한 받아오기
//    fun getLocationPms() {
//        ActivityCompat.requestPermissions(activity, pmsLocation, AppProperties.REQUEST_LOCATION)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.S)
//    fun getBluetoothPms() {
//        ActivityCompat.requestPermissions(activity, pmsBluetooth, AppProperties.REQUEST_ENABLE_BT)
//    }
}