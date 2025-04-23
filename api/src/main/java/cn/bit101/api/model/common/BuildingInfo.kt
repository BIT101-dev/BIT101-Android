package cn.bit101.api.model.common

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BuildingInfo (
    @SerializedName("JXLMC") val buildingName: String,          // 教学楼名, 和另一个字段 JXLJC 大部分时候是一样的
    @SerializedName("JXLDM") val buildingIndex: String,         // 教学楼代码, 有点坑的是可能出现字母
    @SerializedName("XXXQDM_DISPLAY") val campusName: String,   // 校区名
    @SerializedName("XXXQDM") val campusIndex: Int              // 校区代码
) : Serializable