package com.wooriyo.us.pinmenumobileer.model

import com.google.gson.annotations.SerializedName

data class MemberDTO (
    var status : Int,
    var msg : String,
    @SerializedName("useridx") var useridx : Int,
    @SerializedName("userid") var userid : String
)