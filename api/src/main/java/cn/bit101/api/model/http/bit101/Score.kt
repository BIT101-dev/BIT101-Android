package cn.bit101.api.model.http.bit101

// 成绩查询，需要登录
class GetScoreDataModel private constructor() {
    data class Response(
        // 二维表 第一行为表头之后每一行为一门课程
        val data: ArrayList<String>,
    )
}

// 获取可信成绩单，需要登录
class GetScoreReport private constructor() {
    data class Response(
        // 图片链接列表
        val data: ArrayList<String>,
    )
}