package com.wooriyo.us.pinmenumobileer.model

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject

class SharedDTO(context: Context) {
    private  val pref : SharedPreferences = context.getSharedPreferences("PinMenuEE_Mobile_Pref_key", Context.MODE_PRIVATE)
    val gson = Gson()

    fun getMbrDTO(): MemberDTO? {
        val json: String ?= pref.getString("mbrData", "")
        return gson.fromJson(json, MemberDTO::class.java)
    }

    fun setMbrDTO(mbrData: MemberDTO) {
        val json = gson.toJson(mbrData)
        pref.edit().putString("mbrData", json).apply()
    }

    fun getUserIdx(): Int {
        return pref.getInt("useridx", 0)
    }

    fun setUserIdx(useridx: Int) {
        pref.edit().putInt("useridx", useridx).apply()
    }

    fun getPw() : String? {
        return pref.getString("pw", "")
    }

    fun setPw(pw : String) {
        pref.edit().putString("pw", pw).apply()
    }

    fun getToken(): String? {
        return pref.getString("push_token", "")
    }

    fun setToken(push_token: String) {
        pref.edit().putString("push_token", push_token).apply()
    }

    fun getNoPopupDate(): String? {
        return pref.getString("not_today", "")
    }

    fun setNoPopupDate(notToday: String) {
        pref.edit().putString("not_today", notToday).apply()
    }

    fun getPrintSetting() : PrintContentDTO? {
        val json: String ?= pref.getString("printSetting", "")
        return gson.fromJson(json, PrintContentDTO::class.java)
    }

    fun setPrintSetting(printContent: PrintContentDTO) {
        val json = gson.toJson(printContent)
        pref.edit().putString("printSetting", json).apply()
    }

    fun getConnectedPrinter(): JSONObject? {
        val json: String? = pref.getString("printer", "")

        return if(json.isNullOrEmpty()) null else JSONObject(json)
    }

    fun setConnectedPrinter(bluetoothDevice: BluetoothDevice) {
        val json = JSONObject()
        json.put("uuids", bluetoothDevice.uuids)
        json.put("address", bluetoothDevice.address)
        pref.edit().putString("printer", json.toString()).apply()
    }

    fun logout() {
        pref.edit().remove("mbrData").apply()
        pref.edit().remove("useridx").apply()
        pref.edit().remove("pw").apply()
    }
}