package cn.bit101.android.ui.setting2.page

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun PagesSettingPageContent(
    mainController: MainController,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
) {
    var data by remember { mutableStateOf(
        listOf("主页",
            "日历",
            "标签",
            "颜色",
            "关于")
    ) }
    val view = LocalView.current
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            // 触觉反馈
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            data = data.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
    )

    var selectedIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .reorderable(state),
        contentPadding = PaddingValues(16.dp),
    ) {
        itemsIndexed(data, { i, s -> s }) { index, item ->
            ReorderableItem(state = state, key = item) { isDragging ->
                val color = if (isDragging) MaterialTheme.colorScheme.surfaceContainerHigh
                else MaterialTheme.colorScheme.surface
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .detectReorderAfterLongPress(state),
                    color = color,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Row(modifier = Modifier.align(Alignment.CenterStart)) {
                            IconButton(
                                modifier = Modifier.detectReorder(state),
                                onClick = { }
                            ) {
                                Icon(imageVector = Icons.Outlined.Apps, contentDescription = "move")
                            }
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                text = item
                            )
                        }
                        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                            Checkbox(
                                checked = true,
                                onCheckedChange = {}
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            RadioButton(selected = selectedIndex == index, onClick = { selectedIndex = index })
                        }
                    }
                }
            }
        }
    }


//    LazyColumn(
//        modifier = Modifier
//            .nestedScroll(nestedScrollConnection)
//            .padding(paddingValues)
//            .fillMaxSize(),
//        contentPadding = PaddingValues(16.dp),
//    ) {
//
//
//        items(settings) {
//            SettingItem(
//                title = it.title,
//                subTitle = it.subTitle,
//                onClick = it.onClick,
//                suffix = it.suffix,
//            )
//            Spacer(modifier = Modifier.padding(4.dp))
//        }
//    }
}