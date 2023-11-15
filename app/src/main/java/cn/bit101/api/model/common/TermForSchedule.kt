package cn.bit101.api.model.common

import com.google.gson.annotations.SerializedName

data class TermForSchedule(
    @SerializedName("DM") val DM: String, // 学年学期代码
)