package cn.bit101.api.model.http.bit101

import cn.bit101.api.model.common.ReportType
import cn.bit101.api.model.common.User

class PostReportDataModel private constructor() {
    data class Body(
        val obj: String,
        val text: String,
        val typeId: Long
    )
}

class GetReportsDataModel private constructor() {
    data class ResponseItem(
        val createdTime: String,
        val id: String,
        val obj: String,
        val reportType: ReportType,
        val status: Long,
        val text: String,
        val user: User
    )
    class Response : ArrayList<ResponseItem>()
}

class PutReportDataModel private constructor() {
    data class Body(
        val status: Int
    )
}

class GetReportTypesDataModel private constructor() {
    class Response : ArrayList<ReportType>()
}

class PostBanDataModel private constructor() {
    data class Body(
        val uid: Int,
        val time: String
    )
}

class GetBansDataModel private constructor() {
    data class ResponseItem(
        val id: String,
        val time: String,
        val user: User
    )

    class Response : ArrayList<ResponseItem>()
}