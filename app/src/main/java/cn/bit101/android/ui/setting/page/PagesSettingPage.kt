package cn.bit101.android.ui.setting.page

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.manager.base.PageShowOnNav
import cn.bit101.android.manager.base.toNameAndValue
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.setting.SettingItem
import cn.bit101.android.ui.component.setting.SettingItemData
import cn.bit101.android.ui.setting.viewmodel.PageViewModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
private fun PagesSettingPageContent(
    pages: List<PageShowOnNav>,
    homePage: PageShowOnNav,
    hiddenPages: List<PageShowOnNav>,

    onChangePages: (List<PageShowOnNav>, PageShowOnNav, List<PageShowOnNav>) -> Unit,
    onReset: () -> Unit,
) {
    var changeablePages by remember { mutableStateOf(pages) }
    var changeableHomePage by remember { mutableStateOf(homePage) }
    var changeableHiddenPages by remember { mutableStateOf(hiddenPages) }

    val view = LocalView.current
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            // 触觉反馈
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            changeablePages = changeablePages.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.padding(8.dp))
            Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                SettingItem(
                    data = SettingItemData.Card(
                        title = "页面编辑",
                        subTitle = "按下左侧按钮或长按条目可拖动，右侧按钮可设置主页和隐藏，打对勾的会显示在底部的导航栏，" +
                                "单选框选中的在启动App时会作为主页显示。\n\n" +
                                "Tips: 我的页面不能隐藏",
                    ),
                )
            }
            Spacer(modifier = Modifier.padding(4.dp))
            LazyColumn(
                state = state.listState,
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(state),
                contentPadding = PaddingValues(12.dp),
            ) {
                itemsIndexed(changeablePages, { i, s -> s.toNameAndValue().value }) { index, item ->
                    ReorderableItem(state = state, key = item.toNameAndValue().value) { isDragging ->
                        val color = if (isDragging) MaterialTheme.colorScheme.surfaceContainerHigh
                        else MaterialTheme.colorScheme.surface
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .detectReorderAfterLongPress(state)
                                .clip(RoundedCornerShape(8.dp)),
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
                                        Icon(imageVector = Icons.Outlined.DragIndicator, contentDescription = "move")
                                    }
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        text = item.toNameAndValue().name
                                    )
                                }
                                Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                                    Checkbox(
                                        checked = item !in changeableHiddenPages,
                                        onCheckedChange = { changeableHiddenPages = if(it) changeableHiddenPages - item else changeableHiddenPages + item }
                                    )
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    RadioButton(selected = item == changeableHomePage, onClick = { changeableHomePage = item })
                                }
                            }
                        }
                    }
                }
            }
        }


        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            FilledTonalButton(onClick = onReset) {
                Text(text = "重置")
            }
            Spacer(modifier = Modifier.padding(4.dp))
            Button(
                onClick = { onChangePages(changeablePages, changeableHomePage, changeableHiddenPages) }
            ) {
                Text(text = "保存")
            }
        }
    }
}

@Composable
fun PagesSettingPage(
    mainController: MainController,
    vm: PageViewModel = hiltViewModel()
) {

    val pages by vm.allPagesFlow.collectAsState(initial = PageShowOnNav.allPages)
    val homePage by vm.homePageFlow.collectAsState(initial = PageShowOnNav.allPages[0])
    val hiddenPages by vm.hiddenPagesFlow.collectAsState(initial = emptyList())

    PagesSettingPageContent(
        pages = pages,
        homePage = homePage,
        hiddenPages = hiddenPages,
        onChangePages = vm::changePageSettings,
        onReset = vm::reset
    )
}