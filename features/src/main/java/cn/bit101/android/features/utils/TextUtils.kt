package cn.bit101.android.features.utils

object TextUtils {
    fun findUrl(text: String): Sequence<MatchResult> {
        val regex = Regex("https?://[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?[^\\u4E00-\\u9FA5\\s]*")
//        val regex = Regex("/^((ht|f)tps?):\\/\\/([\\w\\-]+(\\.[\\w\\-]+)*\\/)*[\\w\\-]+(\\.[\\w\\-]+)*\\/?(\\?([\\w\\-\\.,@?^=%&:\\/~\\+#]*)+)?/")
        return regex.findAll(text)
    }
}