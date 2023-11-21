package cn.bit101.android.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

object ColorUtils {
    // 判断一个颜色是否是浅色
    fun isLightColor(c: Color): Boolean {
        val color = c.toArgb()
        val red = color shr 16 and 0xFF
        val green = color shr 8 and 0xFF
        val blue = color shr 0 and 0xFF
        val grayLevel = 0.2126 * red + 0.7152 * green + 0.0722 * blue
        return grayLevel >= 192
    }
}