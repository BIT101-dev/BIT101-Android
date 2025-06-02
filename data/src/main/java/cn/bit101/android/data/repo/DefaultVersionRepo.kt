package cn.bit101.android.data.repo

import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.VersionRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultVersionRepo @Inject constructor(
    private val apiManager: APIManager
) : VersionRepo {

    private val api = apiManager.api

    override suspend fun getVersionInfo() = withContext(Dispatchers.IO) {
//        delay(500)
//        GetVersionDataModel.Response(
//            minVersionCode = 5,
//            minVersionName = "2.0.0",
//            msg = "\uD83D\uDEA8⚠️❗由于学校接口变更，老版本无法正常获取课程表，请务必更新。\n\n主要更新内容：\n1. 适配学校课程表接口变更\n2. 修复乐学DDL变化后不更新的问题\n3. 添加自定义DDL功能\n\n新学年快乐喔！\n把BIT101推荐给新同学吧OvO",
//            url = "http://android.bit101.cn/release/BIT101-1.1.0.apk",
//            versionCode = 5,
//            versionName = "2.0.0"
//        )
        api.app.getVersion().body() ?: throw Exception("get version error")
    }
}