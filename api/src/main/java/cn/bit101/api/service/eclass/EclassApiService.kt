package cn.bit101.api.service.eclass

import cn.bit101.api.model.http.eclass.EclassCoursesDataModel
import cn.bit101.api.model.http.eclass.GetEclassActivitiesDataModel
import cn.bit101.api.service.ApiService
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EclassApiService : ApiService {

    @POST("/api/my-courses")
    suspend fun getMyCourses(
        @Body request: EclassCoursesDataModel.Request,
    ): Response<EclassCoursesDataModel.Response>

    @GET("/api/courses/{id}/activities")
    suspend fun getActivities(
        @Path("id") courseId: Int,
    ): Response<GetEclassActivitiesDataModel.Response>
}
