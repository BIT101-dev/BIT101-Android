package cn.bit101.api.model.common

import com.google.gson.annotations.SerializedName

// 空教室查询中学校校区代码和名称的对应关系
data class CampusInfo(
    @SerializedName("MC") val displayName: String,
    @SerializedName("DM") val code: String,
)