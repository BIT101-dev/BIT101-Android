package cn.bit101.android.ui.schedule

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.bit101.android.MainController
import cn.bit101.android.viewmodel.ScheduleViewModel
import cn.bit101.android.viewmodel.getCoursesFromNet
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @author flwfdd
 * @date 2023/4/12 14:29
 * @description _(:з」∠)_
 */

@Composable
fun CourseSchedule(
    mainController: MainController,
    active: Boolean,
    vm: ScheduleViewModel = viewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        var showDialog by rememberSaveable { mutableStateOf(false) }
        val term = vm.termFlow.collectAsState(initial = "").value

        // 没有课程表时term=null 加载之前为空字符串
        if (term?.isNotEmpty() == true) {
            CourseScheduleCalendar(vm, onConfig = { showDialog = true })
        } else {
            if (term == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var loading by remember { mutableStateOf(false) }
                    Button(enabled = !loading, onClick = {
                        MainScope().launch {
                            loading = true
                            getCoursesFromNet()
                            loading = false
                        }
                    }) {
                        if (loading) CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        else Text("获取课程表")
                    }
                }
            }

        }

        // 设置对话框 自定义进入和退出动画
        AnimatedVisibility(
            visible = showDialog,
            enter = slideIn(
                initialOffset = { IntOffset(0, it.height) },
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                )
            ),
            exit = slideOut(
                targetOffset = { IntOffset(0, it.height) },
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                )
            )
        ) {
            CourseScheduleConfigDialog(mainController, vm) {
                showDialog = false
            }
        }

        // 响应返回键 收起设置对话框
        BackHandler(enabled = showDialog && active) {
            showDialog = false
        }
    }
}
