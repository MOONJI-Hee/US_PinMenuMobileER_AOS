package com.wooriyo.us.pinmenumobileer.model

import com.google.gson.annotations.SerializedName

data class PgHistoryDTO(
    @SerializedName("title") var title: String,
    @SerializedName("glist") var pgDetailList: ArrayList<PgDetailDTO>?
)