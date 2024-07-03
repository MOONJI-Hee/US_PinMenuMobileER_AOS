package com.wooriyo.us.pinmenumobileer.model

import com.google.gson.annotations.SerializedName

data class PgDetailResultDTO(
    @SerializedName("status") var status: Int,
    @SerializedName("msg") var msg: String,
    @SerializedName("tableNo") var tableNo: String,
    @SerializedName("cardname") var cardname: String,
    @SerializedName("cardnum") var cardnum: String,
    @SerializedName("amt") var amt: Double,
    @SerializedName("tid") var tid: String,
    @SerializedName("pay_regdt") var pay_regdt: String,
    @SerializedName("pglist") var pgDetailList: ArrayList<PgDetailDTO>
)