package com.wooriyo.us.pinmenumobileer.model

import com.google.gson.annotations.SerializedName

data class PgDetailResultDTO(
    @SerializedName("status") var status: Int,
    @SerializedName("msg") var msg: String,
    @SerializedName("ordNo") var orderNo: String,
    @SerializedName("tableNo") var tableNo: String,
    @SerializedName("cardname") var cardname: String,
    @SerializedName("cardnum") var cardnum: String,
    @SerializedName("amt") var amt: Double,
    @SerializedName("tid") var tid: String,
    @SerializedName("pay_regdt") var pay_regdt: String,
    @SerializedName("tip")      var tip : Double,                       // 팁 총 금액
    @SerializedName("tipPer")   var tipPer : String,                    // 팁 %
    @SerializedName("tax")      var tax : Double,                       // 택스 총 금액
    @SerializedName("taxPer")   var taxPer : String,                    // 택스 %
    @SerializedName("subTotal") var subTotal: String,
    @SerializedName("pglist") var pgDetailList: ArrayList<PgDetailDTO>
)