package cn.bit101.android.features.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.bit101.android.features.common.MainController
import cn.bit101.android.features.schedule.classroom.FreeClassroomSearch
import cn.bit101.android.features.schedule.component.TabPager
import cn.bit101.android.features.schedule.component.TabPagerItem
import cn.bit101.android.features.schedule.course.CourseSchedule
import cn.bit101.android.features.schedule.ddl.DDLSchedule

@Composable
fun ScheduleScreen(mainController: MainController) {
    val items = listOf(TabPagerItem("课表") {
        CourseSchedule(mainController, it)
    }, TabPagerItem("DDL") {
        DDLSchedule(mainController, it)
    }, TabPagerItem("空教室") {
        FreeClassroomSearch(mainController, it)
    })

    Scaffold {
        Box(modifier = Modifier.padding(it)) {
            TabPager(items)
        }
    }
}