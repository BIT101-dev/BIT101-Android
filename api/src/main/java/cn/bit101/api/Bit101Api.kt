package cn.bit101.api

import cn.bit101.api.service.app.AppApiService
import cn.bit101.api.service.bit101.CoursesApiService
import cn.bit101.api.service.bit101.ManageApiService
import cn.bit101.api.service.bit101.MessageApiService
import cn.bit101.api.service.bit101.PapersApiService
import cn.bit101.api.service.bit101.PostersApiService
import cn.bit101.api.service.bit101.ReactionApiService
import cn.bit101.api.service.bit101.ScoreApiService
import cn.bit101.api.service.bit101.UploadApiService
import cn.bit101.api.service.bit101.UserApiService
import cn.bit101.api.service.bit101.VariablesApiService
import cn.bit101.api.service.school.SchoolJxzxehallappApiService
import cn.bit101.api.service.school.SchoolLexueApiService
import cn.bit101.api.service.school.SchoolUserApiService
import retrofit2.Retrofit
import retrofit2.create

class Bit101Api internal constructor(
    bit101Retrofit: Retrofit,
    appRetrofit: Retrofit,
    jwmsRetrofit: Retrofit,
    jwcRetrofit: Retrofit,
    jxzxehallappRetrofit: Retrofit,
    schoolLoginRetrofit: Retrofit,
    lexueRetrofit: Retrofit,
) {
    val courses: CoursesApiService = bit101Retrofit.create()
    val manage: ManageApiService = bit101Retrofit.create()
    val message: MessageApiService = bit101Retrofit.create()
    val papers: PapersApiService = bit101Retrofit.create()
    val posters: PostersApiService = bit101Retrofit.create()
    val reaction: ReactionApiService = bit101Retrofit.create()
    val score: ScoreApiService = bit101Retrofit.create()
    val upload: UploadApiService = bit101Retrofit.create()
    val user: UserApiService = bit101Retrofit.create()
    val variables: VariablesApiService = bit101Retrofit.create()

    val app: AppApiService = appRetrofit.create()

    val schoolJxzxehallapp: SchoolJxzxehallappApiService = jxzxehallappRetrofit.create()
    val schoolUser: SchoolUserApiService = schoolLoginRetrofit.create()

    val schoolLexue: SchoolLexueApiService = lexueRetrofit.create()
}
