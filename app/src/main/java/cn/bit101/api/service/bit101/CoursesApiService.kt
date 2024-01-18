package cn.bit101.api.service.bit101

import cn.bit101.api.model.common.CoursesOrder
import cn.bit101.api.model.common.MaterialType
import cn.bit101.api.model.http.*
import cn.bit101.api.model.http.bit101.GetCourseByIdDataModel
import cn.bit101.api.model.http.bit101.GetCourseHistoriesDataModel
import cn.bit101.api.model.http.bit101.GetCourseUploadUrlDataModel
import cn.bit101.api.model.http.bit101.GetCoursesDataModel
import cn.bit101.api.model.http.bit101.GetScheduleDataModel
import cn.bit101.api.model.http.bit101.PostCourseUploadLogDataModel
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.*

interface CoursesApiService : ApiService {
    @GET("/courses")
    suspend fun getCourses(
        @Query("search") search: String? = null,
        @Query("order") order: String? = null,
        @Query("page") page: Int? = null,
    ): Response<GetCoursesDataModel.Response>

    @GET("/courses/{id}")
    suspend fun getCourseById(
        @Path("id") id: String,
    ): Response<GetCourseByIdDataModel.Response>

    @GET("/courses/upload/url")
    suspend fun getCourseUploadUrl(
        @Query("name") name: String,
        @Query("number") number: String,
        @Query("type") type: MaterialType? = null,
    ): Response<GetCourseUploadUrlDataModel.Response>

    @POST("/courses/upload/log")
    suspend fun postCourseUploadLog(
        @Body body: PostCourseUploadLogDataModel.Body
    ): Response<Void>

    @GET("/courses/schedule")
    suspend fun getSchedule(
        @Header("webvpn-cookie") cookie: String,
    ): Response<GetScheduleDataModel.Response>


    @GET("/courses/histories/{number}")
    suspend fun getCourseHistories(
        @Path("number") number: String,
    ): Response<GetCourseHistoriesDataModel.Response>
}