package cn.bit101.android.features.common.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

object ColorUtils {
    // 判断一个颜色是否是浅色
    fun isLightColor(c: Color) = c.luminance() > 0.5f
}

// 混合两个颜色 第一个颜色占比为ratio
fun mixColor(color1: Color, color2: Color, ratio: Float): Color {
    return Color(
        (color1.red * ratio + color2.red * (1 - ratio)),
        (color1.green * ratio + color2.green * (1 - ratio)),
        (color1.blue * ratio + color2.blue * (1 - ratio))
    )
}