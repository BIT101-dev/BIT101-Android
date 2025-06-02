package cn.bit101.api.model.common

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CourseForSchedule(
    @SerializedName("XNXQDM") var XNXQDM: String?, // 学年学期
    @SerializedName("KCM") var KCM: String?, // 课程名
    @SerializedName("SKJS") var SKJS: String?, // 授课教师 逗号分隔
    @SerializedName("JASMC") var JASMC: String?, // 教室
    @SerializedName("YPSJDD") var YPSJDD: String?, // 上课时空描述
    @SerializedName("SKZC") var SKZC: String?, // 上课周次 一个01串 1表示上课 0表示不上课
    @SerializedName("SKXQ") var SKXQ: Int?, // 星期几
    @SerializedName("KSJC") var KSJC: Int?, // 开始节次
    @SerializedName("JSJC") var JSJC: Int?, // 结束节次
    @SerializedName("XXXQMC") var XXXQMC: String?, // 校区
    @SerializedName("KCH") var KCH: String?, // 课程号
    @SerializedName("XF") var XF: Int?, // 学分
    @SerializedName("XS") var XS: Int?, // 学时
    @SerializedName("KCXZDM_DISPLAY") var KCXZDM_DISPLAY: String?, // 课程性质 必修选修什么的
    @SerializedName("KCLBDM_DISPLAY") var KCLBDM_DISPLAY: String?, // 课程类别 文化课实践课什么的
    @SerializedName("KKDWDM_DISPLAY") var KKDWDM_DISPLAY: String?, // 开课单位
) : Serializable