package cn.bit101.api.model.http.app


//{
//    "version_code":3,
//    "version_name":"1.1.0",
//    "url":"http://android.bit101.cn/release/BIT101-1.1.0.apk",
//    "msg":"🚨⚠️❗由于学校接口变更，老版本无法正常获取课程表，请务必更新。\n\n主要更新内容：\n1. 适配学校课程表接口变更\n2. 修复乐学DDL变化后不更新的问题\n3. 添加自定义DDL功能\n\n新学年快乐喔！\n把BIT101推荐给新同学吧OvO"
//}
class GetVersionDataModel private constructor() {

    data class Response(
        val minVersionCode: Int,
        val minVersionName: String,
        val versionCode: Int,
        val versionName: String,
        val url: String,
        val msg: String,
    )
}

