package cn.bit101.api.model.http.school

import cn.bit101.api.model.common.BuildingInfo
import cn.bit101.api.model.common.ClassroomInfo
import cn.bit101.api.model.common.CourseForSchedule
import cn.bit101.api.model.common.TermForSchedule
import com.google.gson.annotations.SerializedName

class GetCurrentTermDataModel private constructor() {
    data class DQXNXQ(
        @SerializedName("totalSize") val totalSize: Int,
        @SerializedName("pageSize") val pageSize: Int,
        val rows: List<TermForSchedule>,

    )

    data class Datas(
        val dqxnxq: DQXNXQ,

    )

    data class Response(
        val datas: Datas,
        val code: String,
    )
}

class GetTermsDataModel private constructor() {
    data class XNXQCX(
        @SerializedName("totalSize") val totalSize: Int,
        @SerializedName("pageSize") val pageSize: Int,
        val rows: List<TermForSchedule>,
    )

    data class Datas(
        val xnxqcx: XNXQCX,
    )

    data class Response(
        val datas: Datas,
        val code: String,
    )
}

class PostGetWeekAndDateDataModel private constructor() {
    data class Data(
        @SerializedName("XQ") val week: Int, // 星期
        @SerializedName("RQ") val date: String, // 日期
    )

    data class Response(
        val data: List<Data>,
        val code: String,
    )
}

class PostGetScheduleDataModel private constructor() {
    data class CXXSZHXQKB(
        val rows: List<CourseForSchedule>
    )

    data class Datas(
        val cxxszhxqkb: CXXSZHXQKB
    )

    data class Response(
        val datas: Datas
    )
}

class GetBuildingTypeDataModel private constructor() {
    data class CXJXL(
        val rows: List<BuildingInfo>,
    )

    data class Datas(
        val cxjxl: CXJXL
    )

    data class Response(
        val datas: Datas,
    )
}

class GetClassroomDataModel private constructor() {
    data class CXKXJASQK(
        val rows: List<ClassroomInfo>,
    )

    data class Datas(
        val cxkxjasqk: CXKXJASQK
    )

    data class Response(
        val datas: Datas,
    )
}