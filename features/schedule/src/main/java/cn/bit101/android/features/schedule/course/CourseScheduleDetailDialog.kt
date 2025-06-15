package cn.bit101.android.features.schedule.course

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cn.bit101.android.data.database.entity.CourseScheduleEntity
import cn.bit101.android.data.database.entity.ExamScheduleEntity
import cn.bit101.android.features.common.component.schedule.Item
import java.time.temporal.ChronoUnit

// 课程详情对话框
@Composable
internal fun CourseScheduleDetailDialog(
    course: CourseScheduleEntity,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.fillMaxSize(0.9f),
        tonalElevation = 1.dp,
        onDismissRequest = onDismiss,
        title = {
            Text(text = course.name)
        },
        text = {
            val scrollState = rememberScrollState()
            SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                    Item(title = "时空坐标：", content = course.description)
                    Item("课程号：", course.number)
                    Item("授课教师：", course.teacher)
                    Item("学分：", course.credit.toString())
                    Item("学时：", course.hour.toString())
                    Item("课程性质：", course.type)
                    Item("课程类别：", course.category)
                    Item("开课单位：", course.department)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("关闭")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

// 考试详情对话框
@Composable
internal fun ExamScheduleDetailDialog(
    exam: ExamScheduleEntity,
    onDismiss: () -> Unit,
    onAddToCalendar: (ExamScheduleEntity) -> Unit,
) {
    AlertDialog(
        modifier = Modifier.fillMaxSize(0.9f),
        tonalElevation = 1.dp,
        onDismissRequest = onDismiss,
        title = {
            Text(text = "[考试] ${exam.name}")
        },
        text = {
            val scrollState = rememberScrollState()
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Item(title = "考试地点：", content = exam.classroom)
                    Item("考试日期：", exam.date.toString())
                    Item(
                        "考试时间：",
                        "${exam.beginTime} ~ ${exam.endTime} (${
                            exam.beginTime.until(
                                exam.endTime,
                                ChronoUnit.MINUTES
                            )
                        } 分钟)"
                    )
                    Item("课程号：", exam.courseId)
                    Item("授课教师：", exam.teacher)
                    Item("座位号：", exam.seatId)
                    Item("考试模式：", exam.examMode)

                    Spacer(modifier = Modifier.padding(2.dp))

                    AssistChip(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        shape = MaterialTheme.shapes.medium,
                        border = null,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                alpha = 0.5f
                            )
                        ),
                        onClick = { onAddToCalendar(exam) },
                        label = {
                            Text(
                                "添加到系统日历",
                                Modifier.padding(vertical = 10.dp)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = "Add to system calendar",
                                Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("关闭")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailPreview() {
    var show by remember { mutableStateOf(false) }
    CourseScheduleDetailDialog(
        CourseScheduleEntity(
            0,
            "2020-2021-2",
            "高等数学",
            "张三,李四",
            "教学楼101",
            "第1-16周",
            "[1][2][3][4][5][6][7][8]",
            1,
            1,
            2,
            "良乡校区",
            "123456",
            4,
            64,
            "必修",
            "文化课",
            "数学系"
        ),
        onDismiss = { show = false }
    )
}

