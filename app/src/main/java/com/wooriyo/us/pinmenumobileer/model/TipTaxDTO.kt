package com.wooriyo.us.pinmenumobileer.model

import com.google.gson.annotations.SerializedName

data class TipTaxDTO(
    @SerializedName("status") var status: Int,
    @SerializedName("msg") var msg: String,
    @SerializedName("storeidx") var storeidx: Int,
    @SerializedName("tipuse") var tipuse: String,
    @SerializedName("tip1") var tip1: Int,
    @SerializedName("tip2") var tip2: Int,
    @SerializedName("tip3") var tip3: Int,
    @SerializedName("tip4") var tip4: Int,
    @SerializedName("tax") var tax: String
)