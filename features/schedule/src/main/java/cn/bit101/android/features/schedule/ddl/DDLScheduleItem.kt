package cn.bit101.android.features.schedule.ddl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.bit101.android.data.database.entity.DDLScheduleEntity

/**
 * @author flwfdd
 * @date 15/05/2023 00:39
 * @description 单个日程项目卡片
 * _(:з」∠)_
 */

@Composable
internal fun DDLScheduleItem(modifier: Modifier, item: DDLScheduleEntity, vm: DDLScheduleViewModel) {
    Surface(
        modifier = modifier,
        color = if (item.done) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else mixColor(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.errorContainer,
            vm.remainTimeRatio(item.time)
        ),
        contentColor = if (item.done) MaterialTheme.colorScheme.onSecondaryContainer else mixColor(
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.onErrorContainer,
            vm.remainTimeRatio(item.time)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 15.dp, 5.dp, 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
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
                    text = vm.remainTime(item.time),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 是否完成
            Checkbox(checked = item.done, onCheckedChange = {
                vm.setDone(item, it)
            })
        }
    }
}

// 混合两个颜色 第一个颜色占比为ratio
private fun mixColor(color1: Color, color2: Color, ratio: Float): Color {
    return Color(
        (color1.red * ratio + color2.red * (1 - ratio)),
        (color1.green * ratio + color2.green * (1 - ratio)),
        (color1.blue * ratio + color2.blue * (1 - ratio))
    )
}
