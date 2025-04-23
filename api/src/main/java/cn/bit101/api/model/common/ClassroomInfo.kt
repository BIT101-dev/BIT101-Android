package cn.bit101.api.model.common

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ClassroomInfo (
    @SerializedName("JASMC") val classroomName: String,
    @SerializedName("ZYJC") val busyTimeStr: String?       // 非常坑的一点是这个可以是 null
) : Serializable