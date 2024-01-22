package cn.bit101.android.features.schedule

import androidx.compose.runtime.Composable
import cn.bit101.android.features.MainController
import cn.bit101.android.features.schedule.course.CourseSchedule
import cn.bit101.android.features.schedule.ddl.DDLSchedule
import cn.bit101.android.features.component.TabPager
import cn.bit101.android.features.component.TabPagerItem

@Composable
fun ScheduleScreen(
    mainController: MainController,
) {
    val items = listOf(TabPagerItem("课表") {
        CourseSchedule(mainController, it)
    }, TabPagerItem("DDL") {
        DDLSchedule(mainController, it)
    })

    TabPager(items)
}