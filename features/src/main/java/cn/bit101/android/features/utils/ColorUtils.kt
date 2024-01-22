package cn.bit101.android.features.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

object ColorUtils {
    // 判断一个颜色是否是浅色
    fun isLightColor(c: Color) = c.luminance() > 0.5f
}