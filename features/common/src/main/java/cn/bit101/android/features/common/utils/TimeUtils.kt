package cn.bit101.android.features.common.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.time.LocalTime
import java.util.Timer
import java.util.TimerTask

// 获取当前时间, 在组合函数内部使用, 能自动更新时间
// (直接使用 LocalTime.now() 会无法触发重组)
// 每分钟更新一次, 整点更新 (比如 12:37:08 调用, 则下一次更新是在 12:38:00)
@Composable
fun getCurrentTime(): LocalTime {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    val lifecycleOwner = LocalLifecycleOwner.current

    class UpdateTask : TimerTask() {
        override fun run() {
            currentTime = LocalTime.now()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val timer = Timer()

        var updateTime = UpdateTask()

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    updateTime.run()    // 考虑到用户可能离开了比较久 (或者出去改时间了 XD), 直接更新一次
                    timer.schedule(updateTime, (60 - LocalTime.now().second)  * 1000L, 60 * 1000)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    updateTime.cancel()
                    updateTime = UpdateTask()   // 取消后的任务不能再次使用, 只能新建一次
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            timer.cancel()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return currentTime
}