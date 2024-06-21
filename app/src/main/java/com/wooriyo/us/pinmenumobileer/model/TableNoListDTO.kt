package com.wooriyo.us.pinmenumobileer.model

import com.google.gson.annotations.SerializedName

data class TableNoListDTO(
    @SerializedName("status") var status : Int,
    @SerializedName("msg") var msg : String,
    @SerializedName("tableNoList") var tableNoList : ArrayList<TableNoDTO>
)
