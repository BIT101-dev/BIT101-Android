package cn.bit101.android.features.schedule.course

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.data.database.entity.CustomScheduleEntity
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.common.component.schedule.AddEditScheduleDialog
import cn.bit101.android.features.common.component.schedule.CustomScheduleDetailDialog
import cn.bit101.android.features.common.helper.successMessage
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.nav.NavDest

/**
 * @author flwfdd
 * @date 2023/4/12 14:29
 * @description 课程表主页面
 * _(:з」∠)_
 */

@Composable
internal fun CourseSchedule(
    mainController: MainController,
    active: Boolean,
    vm: CourseScheduleViewModel = hiltViewModel()
) {
    /**
     * 当前选择的学期
     */
    val term by vm.currentTermFlow.collectAsState(initial = null)

    /**
     * 日程数据
     */
    val schedules by vm.schedules.collectAsState()

    /**
     * 当前周
     */
    val week by vm.weekFlow.collectAsState()

    /**
     * 学期开始日期
     */
    val firstDay by vm.firstDayFlow.collectAsState(initial = null)

    /**
     * 课表相关配置
     */
    val showDivider by vm.showDividerFlow.collectAsState(initial = null)
    val showSaturday by vm.showSaturdayFlow.collectAsState(initial = null)
    val showSunday by vm.showSundayFlow.collectAsState(initial = null)
    val showHighlightToday by vm.showHighlightTodayFlow.collectAsState(initial = null)
    val showBorder by vm.showBorderFlow.collectAsState(initial = null)

    /**
     * 时间表字符串
     */
    val timeTable by vm.timeTableStringFlow.collectAsState(initial = null)

    /**
     * 显示当前时间线
     */
    val currentTime by vm.showCurrentTimeFlow.collectAsState(initial = null)

    /**
     * 显示考试信息
     */
    val showExamInfo by vm.showExamInfoFlow.collectAsState(initial = null)

    val showCourseDetailState by vm.showCourseDetail.collectAsState()

    val showExamDetailState by vm.showExamDetail.collectAsState()

    val showCustomScheduleState by vm.showCustomScheduleDetail.collectAsState()

    var showAddScheduleDialog by remember { mutableStateOf(false) }

    var nowEditCustomSchedule by remember { mutableStateOf<CustomScheduleEntity?>(null) }

    val refreshState by vm.refreshState.collectAsState()

    val addCustomScheduleState by vm.addEditCustomScheduleStateLiveData.observeAsState()

    val deleteCustomScheduleState by vm.deleteCustomScheduleStateLiveData.observeAsState()

    val addScheduleToSysCalendarState by vm.addScheduleToSysCalendarStateLiveData.observeAsState()

    val context = LocalContext.current

    LaunchedEffect(refreshState) {
        (refreshState as? SimpleErrorDataState.Success)?.data?.let {
            mainController.snackbar(it.successMessage())
        }
    }

    LaunchedEffect(addCustomScheduleState) {
        if(addCustomScheduleState == SimpleState.Success) {
            if(nowEditCustomSchedule == null) {
                mainController.snackbar("添加成功OvO")
            } else {
                mainController.snackbar("修改成功OvO")
            }

            showAddScheduleDialog = false
            vm.clearCustomScheduleDetail()
        } else if(addCustomScheduleState == SimpleState.Fail) {
            if(nowEditCustomSchedule == null) {
                mainController.snackbar("添加失败Orz")
            } else {
                mainController.snackbar("修改失败Orz")
            }
        }

        vm.addEditCustomScheduleStateLiveData.value = null  // 不清空的话, 连续操作时很可能只有第一次有提示 (因为速度很快, 状态更新不过来)
    }

    LaunchedEffect(deleteCustomScheduleState) {
        if(deleteCustomScheduleState == SimpleState.Success) {
            mainController.snackbar("删除成功OvO")
            vm._showCustomScheduleDetail.value = null
        } else if(deleteCustomScheduleState == SimpleState.Fail) {
            mainController.snackbar("删除失败Orz")
        }
    }

    LaunchedEffect(addScheduleToSysCalendarState) {
        if(addScheduleToSysCalendarState == SimpleState.Fail) {
            mainController.snackbar("添加失败Orz")
        }
        vm.addScheduleToSysCalendarStateLiveData.value = null
    }

    DisposableEffect(Unit) {
        onDispose {
            vm.addEditCustomScheduleStateLiveData.value = null
            vm.deleteCustomScheduleStateLiveData.value = null
            vm.addScheduleToSysCalendarStateLiveData.value = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val failure = refreshState as? SimpleErrorDataState.Fail
        val hasCalendar = !term.isNullOrBlank() && week != Int.MAX_VALUE && firstDay != null
        if (failure != null && hasCalendar) {
            CourseRefreshError(failure.msg, onRetry = { vm.forceRefreshCourses() })
        }
        Box(modifier = Modifier.weight(1f)) {
            if(
                term == null ||
                showDivider == null ||
                showSaturday == null ||
                showSunday == null ||
                showHighlightToday == null ||
                showBorder == null ||
                currentTime == null ||
                showExamInfo == null ||
                timeTable == null
            ) { }
            else if(
                term!!.isEmpty() ||
                week == Int.MAX_VALUE ||
                firstDay == null
            ) {
                // datastore数据加载完了，但是数据库中没有数据，应该显示一个按钮，点击后获取
                // 这里如果没有学期数据、课程数据，就显示一个按钮，点击后获取
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (failure != null) {
                        CourseRefreshError(failure.msg, onRetry = { vm.forceRefreshCourses() })
                    } else {
                        Button(
                            enabled = refreshState !is SimpleErrorDataState.Loading,
                            onClick = vm::forceRefreshCourses
                        ) {
                            if (refreshState is SimpleErrorDataState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else Text("获取课程表")
                        }
                    }
                }
            }
            else {
                val settingData = SettingData(
                    showDivider = showDivider!!,
                    showSaturday = showSaturday!!,
                    showSunday = showSunday!!,
                    showHighlightToday = showHighlightToday!!,
                    showBorder = showBorder!!,
                    showCurrentTime = currentTime!!,
                    showExamInfo = showExamInfo!!
                )

                // 数据加载完毕
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 日程表
                    CourseScheduleCalendar(
                        schedules = schedules,
                        week = week,
                        firstDay = firstDay!!,
                        timeTable = timeTable!!,
                        settingData = settingData,

                        onConfig = { mainController.navigate(NavDest.Setting("calendar")) },
                        onChangeWeek = { vm.changeWeek(it) },
                        onAddSchedule = {
                            nowEditCustomSchedule = null
                            showAddScheduleDialog = true
                        },
                    )
                    if(showCourseDetailState != null) {
                        // 课程详情对话框
                        CourseScheduleDetailDialog(
                            course = showCourseDetailState!!,
                            onDismiss = vm::clearShowCourseDetail
                        )
                    }
                    if(showExamDetailState != null) {
                        // 考试详情对话框
                        ExamScheduleDetailDialog(
                            exam = showExamDetailState!!,
                            onDismiss = vm::clearShowExamDetail,
                            onAddToCalendar = { vm.addScheduleToSysCalendar(context, it) }
                        )
                    }
                    if(showCustomScheduleState != null) {
                        // 自定义日程详情对话框
                        CustomScheduleDetailDialog(
                            schedule = showCustomScheduleState!!,
                            onDismiss = vm::clearCustomScheduleDetail,
                            onEdit = {
                                nowEditCustomSchedule = showCustomScheduleState!!
                                showAddScheduleDialog = true
                            },
                            onDelete = vm::deleteCustomSchedule,
                            onAddToCalendar = { vm.addScheduleToSysCalendar(context, it) }
                        )
                    }
                    if(showAddScheduleDialog) {
                        // 添加 / 修改自定义日程对话框
                        AddEditScheduleDialog(
                            schedule = nowEditCustomSchedule,
                            onAddEditSchedule = {
                                if (nowEditCustomSchedule == null) {
                                    vm.addCustomSchedule(it)
                                } else {
                                    vm.updateCustomSchedule(nowEditCustomSchedule!!, it)
                                }
                            },
                            onDismiss = { showAddScheduleDialog = false },
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun CourseRefreshError(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Text("暂时无法获取课表", style = MaterialTheme.typography.titleSmall)
            }
            Text(message, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilledTonalButton(onClick = onRetry, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("重试")
            }
        }
    }
}
