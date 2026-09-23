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
import cn.bit101.api.service.eclass.EclassApiService
import cn.bit101.api.service.school.SchoolClassroomService
import cn.bit101.api.service.school.SchoolJxzxehallService
import cn.bit101.api.service.school.SchoolLexueService
import cn.bit101.api.service.school.SchoolLoginService
import cn.bit101.api.helper.Logger
import cn.bit101.api.helper.emptyLogger
import retrofit2.Retrofit
import retrofit2.create

class Bit101Api internal constructor(
    bit101Retrofit: Retrofit,
    appRetrofit: Retrofit,
    jwmsRetrofit: Retrofit,
    jwcRetrofit: Retrofit,
    eclassRetrofit: Retrofit,
    logger: Logger = emptyLogger,
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

    val schoolLogin = SchoolLoginService(logger)

    val schoolClassroom = SchoolClassroomService(logger)

    val schoolJxzxehall = SchoolJxzxehallService(logger)

    val schoolLexue = SchoolLexueService(logger)

    /**
     * 课程中心（eclass / 延河课堂）—— 学校 2026 年起用它替代乐学下发作业与学习资料。
     *
     * 与 `schoolLexue` 不同，它不是 BIT-Login SDK 的服务类，而是标准 Retrofit 接口：
     * 会话是纯 cookie（用户 WebView 登录后同步过来），走 `schoolClient` 即可。
     */
    val eclass: EclassApiService = eclassRetrofit.create()
}
