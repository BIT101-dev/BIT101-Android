package cn.bit101.api.model.http.eclass

import com.google.gson.annotations.SerializedName

class EclassCoursesDataModel private constructor() {

    data class Request(
        val page: Int,
        @SerializedName("page_size") val pageSize: Int = 100,
        val fields: String = "id,name",
        val conditions: Map<String, String> = emptyMap(),
    )

    data class Response(
        val courses: List<Course> = emptyList(),
        val page: Int? = null,
        val pages: Int? = null,
    )

    data class Course(
        val id: Int,
        val name: String,
    )
}

class GetEclassActivitiesDataModel private constructor() {

    data class Response(
        val activities: List<Activity> = emptyList(),
    )

    data class Activity(
        val id: Int,
        val title: String,
        val type: String? = null,
        val courseId: Int = 0,
        val startTime: String? = null,
        val endTime: String? = null,
        val visibleEndAt: String? = null,
        val isClosed: Boolean = false,
        val submitTimes: Int? = null,
        val lateSubmissionCount: Int? = null,
        val isReviewHomework: Boolean? = null,
    )
}
