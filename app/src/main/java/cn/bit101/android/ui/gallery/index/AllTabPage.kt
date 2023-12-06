package cn.bit101.android.ui.gallery.index

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cn.bit101.android.ui.MainController
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.NameAndValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllTabPage(
    mainController: MainController,

    /**
     * 嵌套滚动
     */
    nestedScrollConnection: NestedScrollConnection? = null,

    /**
     * 底部的padding
     */
    navBarHeight: Dp,

    /**
     * 最热的帖子状态
     */
    hotPostersState: PostersState,

    /**
     * 最新的帖子状态
     */
    newestPostersState: PostersState,

    /**
     * 打开图片组
     */
    onOpenImages: (Int, List<Image>) -> Unit,

    /**
     * 打开帖子
     */
    onOpenPoster: (Long) -> Unit,

    /**
     * 打开发帖或者编辑帖子的界面
     */
    onOpenPostOrEdit: () -> Unit,
) {
    val nameAndValues = listOf(
        NameAndValue("最新", newestPostersState),
        NameAndValue("最热", hotPostersState),
    )

    val horizontalPagerState = rememberPagerState(
        pageCount = { nameAndValues.size },
        initialPage = 0,
    )

    val scope = rememberCoroutineScope()

    Column(
        modifier = if(nestedScrollConnection != null) Modifier.nestedScroll(nestedScrollConnection)
        else Modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "所有帖子",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            TabRow(
                modifier = Modifier
                    .width(100.dp)
                    .clip(CircleShape),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                selectedTabIndex = horizontalPagerState.currentPage,
                indicator = { tabPositions ->
                    if (horizontalPagerState.currentPage < tabPositions.size) {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[horizontalPagerState.currentPage])
                                .padding(2.dp)
                                .clip(CircleShape)
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface)
                                .zIndex(-1f)
                        )
                    }
                },
                divider = {},
            ) {
                nameAndValues.forEachIndexed { index, data ->
                    Tab(
                        modifier = Modifier.padding(vertical = 6.dp),
                        selected = index == horizontalPagerState.currentPage,
                        onClick = { scope.launch { horizontalPagerState.scrollToPage(index) } },
                        //禁用水波纹特效
                        interactionSource = remember {
                            object : MutableInteractionSource {
                                override val interactions: Flow<Interaction> = emptyFlow()
                                override suspend fun emit(interaction: Interaction) {}
                                override fun tryEmit(interaction: Interaction) = true
                            }
                        },
                    ) {
                        Text(
                            text = data.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (index == horizontalPagerState.currentPage) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                        )
                    }
                }
            }
        }
        HorizontalPager(
            state = horizontalPagerState,
            userScrollEnabled = false
        ) {
            PostersTabPage(
                mainController = mainController,
                navBarHeight = navBarHeight,
                postersState = nameAndValues[it].value,
                onOpenImages = onOpenImages,
                onOpenPoster = onOpenPoster,
                onOpenPostOrEdit = onOpenPostOrEdit,
            )
        }
    }
}