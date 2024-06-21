package com.wooriyo.us.pinmenumobileer.model

import com.google.gson.annotations.SerializedName

data class StoreListDTO(
    var status : Int,
    var msg : String,
    @SerializedName("totalrows") var total : Int,
    @SerializedName("storelist") var storeList : ArrayList<StoreDTO>
)
