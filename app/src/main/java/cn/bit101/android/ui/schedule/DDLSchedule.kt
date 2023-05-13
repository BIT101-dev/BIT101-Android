package cn.bit101.android.ui.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.bit101.android.MainController
import cn.bit101.android.database.DDLScheduleEntity
import cn.bit101.android.viewmodel.DDLScheduleViewModel
import cn.bit101.android.viewmodel.remainTime
import cn.bit101.android.viewmodel.remainTimeRatio
import cn.bit101.android.viewmodel.updateLexueCalendarUrl
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @author flwfdd
 * @date 13/05/2023 15:57
 * @description _(:з」∠)_
 */


@Composable
fun DDLSchedule(
    mainController: MainController,
    active: Boolean,
    vm: DDLScheduleViewModel = viewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 判断是否已经有订阅链接
        val url = vm.lexueCalendarUrlFlow.collectAsState(initial = null)
        if (url.value == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    MainScope().launch {
                        updateLexueCalendarUrl()
                    }
                }) {
                    Text("获取乐学日历")
                }
            }
        } else {
            val events = vm.eventsFlow.collectAsState(initial = emptyList())

            LazyColumn {
                itemsIndexed(events.value) { _, item ->
                    DDLScheduleItem(item, vm)
                }
            }
        }

    }
}

fun mixColor(color1: Color, color2: Color, ratio: Float): Color {
    return Color(
        (color1.red * ratio + color2.red * (1 - ratio)),
        (color1.green * ratio + color2.green * (1 - ratio)),
        (color1.blue * ratio + color2.blue * (1 - ratio))
    )
}

@Composable
fun DDLScheduleItem(item: DDLScheduleEntity, vm: DDLScheduleViewModel) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp, 5.dp),
        color = if (item.done) MaterialTheme.colorScheme.secondaryContainer else mixColor(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.errorContainer,
            remainTimeRatio(item.time)
        ),
        contentColor = if (item.done) MaterialTheme.colorScheme.onSecondaryContainer else mixColor(
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.onErrorContainer,
            remainTimeRatio(item.time)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(15.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = remainTime(item.time),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Checkbox(checked = item.done, onCheckedChange = {
                vm.setDone(item, it)
            })
        }

    }
}
