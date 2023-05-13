package cn.bit101.android.net.school

import android.util.Log
import cn.bit101.android.net.HttpClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * @author flwfdd
 * @date 2023/3/30 21:18
 * @description 课程表网络接口
 */

private data class NowTermResponseRoot(
    var datas: NowTermResponseDatas
)

private data class NowTermResponseDatas(
    var dqxnxq: NowTermResponseDqxnxq
)

private data class NowTermResponseDqxnxq(
    var rows: List<TermResponseItem>
)

private data class TermListResponseRoot(
    var datas: TermListResponseDatas
)

private data class TermListResponseDatas(
    var xnxqcx: TermListResponseXnxqcx
)

private data class TermListResponseXnxqcx(
    var rows: List<TermResponseItem>
)

data class TermResponseItem(
    var DM: String, // 学年学期代码
)

private data class DateResponseRoot(
    var data: List<DateResponseItem>
)

private data class DateResponseItem(
    var XQ: Int, // 星期
    var RQ: String, // 日期
)

private data class CourseResponseRoot(
    var datas: CourseResponseDatas
)

private data class CourseResponseDatas(
    var cxxszhxqkb: CourseResponseCxxszhxqkb
)

private data class CourseResponseCxxszhxqkb(
    var rows: List<CourseResponseItem>
)

data class CourseResponseItem(
    var XNXQDM: String?, // 学年学期
    var KCM: String?, // 课程名
    var SKJS: String?, // 授课教师 逗号分隔
    var JASMC: String?, // 教室
    var YPSJDD: String?, // 上课时空描述
    var SKZC: String?, // 上课周次 一个01串 1表示上课 0表示不上课
    var SKXQ: Int?, // 星期几
    var KSJC: Int?, // 开始节次
    var JSJC: Int?, // 结束节次
    var XXXQMC: String?, // 校区
    var KCH: String?, // 课程号
    var XF: Int?, // 学分
    var XS: Int?, // 学时
    var KCXZDM_DISPLAY: String?, // 课程性质 必修选修什么的
    var KCLBDM_DISPLAY: String?, // 课程类别 文化课实践课什么的
    var KKDWDM_DISPLAY: String?, // 开课单位
)

data class CourseScheduleResponse(
    val term: String,
    var firstDay: LocalDate,
    var courseList: List<CourseResponseItem>
)

suspend fun getCourseSchedule(_term: String = ""): CourseScheduleResponse? {
    var term = _term
    try {
        return withContext(Dispatchers.IO) {
            val client = HttpClient.client
            // 鉴权初始化
            val initRequest = Request.Builder()
                .url(scheduleInitUrl)
                .build()
            client.newCall(initRequest).execute().close()

            // 语言初始化
            val langRequest = Request.Builder()
                .url(scheduleLangUrl)
                .build()
            client.newCall(langRequest).execute().close()

            // 获取学期
            if (term == "") {
                val termRequest = Request.Builder()
                    .url(scheduleNowTermUrl)
                    .build()
                client.newCall(termRequest).execute().use { response ->
                    val res =
                        Gson().fromJson(response.body?.string(), NowTermResponseRoot::class.java)
                    term = res.datas.dqxnxq.rows[0].DM
                }
            }

            // 获取学期开始日期
            var firstDay: LocalDate? = null
            val dateBody = FormBody.Builder()
                .add("requestParamStr", "{\"XNXQDM\":\"$term\",\"ZC\":\"1\"}")
                .build()
            val dateRequest = Request.Builder()
                .url(scheduleDateUrl)
                .post(dateBody)
                .build()
            client.newCall(dateRequest).execute().use { response ->
                val res = Gson().fromJson(response.body?.string(), DateResponseRoot::class.java)
                for (item in res.data) {
                    if (item.XQ == 1) {
                        firstDay =
                            LocalDate.parse(item.RQ, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        break
                    }
                }
            }
            if (firstDay == null) {
                return@withContext null
            }

            // 获取课程表
            val body = FormBody.Builder()
                .add("XNXQDM", term)
                .build()
            val scheduleRequest = Request.Builder()
                .url(scheduleUrl)
                .post(body)
                .build()
            client.newCall(scheduleRequest).execute().use { response ->
                val res = Gson().fromJson(response.body?.string(), CourseResponseRoot::class.java)
                println(res.datas.cxxszhxqkb.rows)
                return@withContext CourseScheduleResponse(
                    term,
                    firstDay!!,
                    res.datas.cxxszhxqkb.rows
                )
            }
        }
    } catch (e: Exception) {
        Log.e("SchoolSchedule", "Get Course Schedule Error $e")
        return null
    }
}

suspend fun getTermList(): List<TermResponseItem> {
    try {
        return withContext(Dispatchers.IO) {
            val client = HttpClient.client

            // 鉴权初始化
            val initRequest = Request.Builder()
                .url(scheduleInitUrl)
                .build()
            client.newCall(initRequest).execute().close()

            // 获取学期列表
            val termRequest = Request.Builder()
                .url(scheduleTermListUrl)
                .build()
            client.newCall(termRequest).execute().use { response ->
                val res = Gson().fromJson(response.body?.string(), TermListResponseRoot::class.java)
                return@withContext res.datas.xnxqcx.rows
            }
        }
    } catch (e: Exception) {
        Log.e("SchoolSchedule", "Get Course Schedule Term List Error $e")
        return emptyList()
    }
}
