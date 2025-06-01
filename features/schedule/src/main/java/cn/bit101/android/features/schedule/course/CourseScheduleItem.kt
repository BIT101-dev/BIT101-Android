package cn.bit101.android.features.schedule.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * @author flwfdd
 * @date 15/05/2023 00:35
 * @description 在课程表上显示的单个课程卡片
 * _(:з」∠)_
 */


@Composable
internal fun CourseScheduleItem(
    modifier: Modifier,
    week: Int,
    item: ScheduleItem,
    color: ScheduleItemColor
) {
    val alpha = remember(item, week) { Math.random().toFloat() * 0.5f + 0.5f }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.containerColor.copy(alpha = alpha),
            contentColor = color.contextColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 课程名
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 0.75),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // 教室
            Box(contentAlignment = Alignment.BottomCenter) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 0.75),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
    }
}
