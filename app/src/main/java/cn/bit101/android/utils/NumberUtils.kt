package cn.bit101.android.utils

object NumberUtils {
    fun format(number: Int): String {
        return when {
            number < 1000 -> number.toString()
            number < 1000000 -> "${number / 10000}w"
            else -> "100w+"
        }
    }
}