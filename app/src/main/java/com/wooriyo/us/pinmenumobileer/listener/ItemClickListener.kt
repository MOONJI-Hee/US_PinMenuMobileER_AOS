package com.wooriyo.us.pinmenumobileer.listener

import android.content.Intent
import android.widget.CheckBox
import com.wooriyo.us.pinmenumobileer.model.StoreDTO

interface ItemClickListener {
    fun onItemClick(position:Int) {}
    fun onItemMove(fromPos: Int, toPos: Int) {}
    fun onQrClick(position: Int, status: Boolean) {}
    fun onStoreClick(storeDTO: StoreDTO, intent: Intent) {}
    fun onCheckClick(position: Int, v: CheckBox, isChecked : Boolean) {}
}