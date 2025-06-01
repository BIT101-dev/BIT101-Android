package cn.bit101.api.model.common

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ExamInfo (
    @SerializedName("JASMC") val location: String?,             // 考试地点 (另有一个名为 JASYWMC 的字段, 我没发现和这个之间有任何区别)
    @SerializedName("KSSJMS") val time: String,                 // 考试时间 (含日期, 星期)
    @SerializedName("KSRQ") val date: String,                   // 考试日期, 附带当天零点的时间
    @SerializedName("ZWH") val seatId: String?,                 // 疑似座位号
    @SerializedName("KSMC") val ksmc: String?,                  // 分散考试 / 集中考试等, 我也不知道这个缩写代表什么
    @SerializedName("XNXQDM_DISPLAY") val termCode: String?,    // 学年学期代码, 显示格式
    @SerializedName("KCM") val courseCode: String?,             // 考试科目, 还有课程号
    @SerializedName("ZJJSXM") val teacherName: String?,         // 授课老师名字
    @SerializedName("KCH") val kch: String?,                    // 课程号
) : Serializable