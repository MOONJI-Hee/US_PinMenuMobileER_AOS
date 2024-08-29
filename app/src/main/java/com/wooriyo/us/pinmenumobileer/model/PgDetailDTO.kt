package com.wooriyo.us.pinmenumobileer.model

import com.google.gson.annotations.SerializedName

data class PgDetailDTO(
    @SerializedName("idx") var idx: Int,
    @SerializedName("name") var name: String,
    @SerializedName("gea") var gea: Int,
    @SerializedName("price") var price: Double,
    @SerializedName("cancel") var cancel: Int,
    @SerializedName("cardname") var cardname: String,
    @SerializedName("cardnum") var cardnum: String,
    @SerializedName("storeidx") var storeidx: Int,
    @SerializedName("tableNo") var tableNo: String,
    @SerializedName("ordcode_key") var ordcode_key: String,
    @SerializedName("tid") var tid: String,
    @SerializedName("regdt") var regdt: String,
    @SerializedName("opt") var opt: String,     // opt1 / opt2 / opt3 ...
    @SerializedName("optlist") var optList: ArrayList<OrderOptDTO>
)