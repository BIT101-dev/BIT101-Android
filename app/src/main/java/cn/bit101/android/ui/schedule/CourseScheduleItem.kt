package cn.bit101.android.ui.schedule

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.bit101.android.database.CourseScheduleEntity

/**
 * @author flwfdd
 * @date 15/05/2023 00:35
 * @description _(:з」∠)_
 */

// 单个课程卡片
@Composable
fun CourseScheduleItem(modifier: Modifier, course: CourseScheduleEntity) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                alpha = Math.random().toFloat() * 0.5f + 0.5f
            ),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 0.75),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(contentAlignment = Alignment.BottomCenter) {
                Text(
                    text = course.classroom,
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 0.75),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
    }
}
