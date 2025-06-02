package cn.bit101.api.model.http.bit101

import cn.bit101.api.model.common.Course
import cn.bit101.api.model.common.CourseDetail

// 获取课程列表
class GetCoursesDataModel private constructor() {
    class Response : ArrayList<Course>()
}

// 获取课程详细信息，需要登录
class GetCourseByIdDataModel private constructor() {
    data class Response(
        val data: CourseDetail,
    )
}

// 获取课程资料上传链接
class GetCourseUploadUrlDataModel private constructor() {
    data class Response(
        val url: String,
        val id: Int,
    )
}

class PostCourseUploadLogDataModel private constructor() {
    data class Body(
        val id: Int,
        val msg: String? = null,
    )
}

class GetScheduleDataModel private constructor() {
    data class Response(
        val url: String,
        val note: String,
    )
}

class GetCourseHistoriesDataModel private constructor() {
    data class ResponseItem(
        val avgScore: Double,
        val maxScore: Double,
        val studentNum: Int,
        val term: String
    )
    class Response : ArrayList<ResponseItem>()
}