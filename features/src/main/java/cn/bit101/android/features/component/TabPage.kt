package cn.bit101.android.features.component

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.OverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

/**
 * @author flwfdd
 * @date 2023/3/16 20:09
 * @description 可以左右滑动切换的TabPager
 */

data class TabPagerItem(
    val title: String,
    val content: @Composable (active: Boolean) -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabPager(items: List<TabPagerItem>) {
    val pagerSate = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { items.size },
    )
    val scope = rememberCoroutineScope() //供动画调用协程
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        FancyAnimatedIndicator(
            tabPositions = tabPositions,
            selectedTabIndex = pagerSate.currentPage
        )
    }

    Column {
        TabRow(
            selectedTabIndex = pagerSate.currentPage,
            indicator = indicator,
        ) {
            items.forEachIndexed { index, item ->
                Tab(
                    selected = pagerSate.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerSate.animateScrollToPage(
                                index
                            )
                        }
                    },
                    text = {
                        Text(

                            text = item.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    modifier = Modifier
                        .padding(5.dp)
                        .clip(MaterialTheme.shapes.medium),
                    //禁用水波纹特效
                    interactionSource = remember {
                        object : MutableInteractionSource {
                            override val interactions: Flow<Interaction> = emptyFlow()
                            override suspend fun emit(interaction: Interaction) {}
                            override fun tryEmit(interaction: Interaction) = true
                        }
                    },
                )
            }
        }

        //禁用overscroll阴影效果
        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            HorizontalPager(
                state = pagerSate,
            ) { index ->
                CompositionLocalProvider(LocalOverscrollConfiguration provides OverscrollConfiguration()) {
                    items[index].content(pagerSate.currentPage == index)
                }
            }
        }
    }
}


@Composable
fun FancyAnimatedIndicator(tabPositions: List<TabPosition>, selectedTabIndex: Int) {
    val transition = updateTransition(selectedTabIndex, label = "tab index")
    val indicatorStart by transition.animateDp(
        transitionSpec = {
            // 使得指示器在切换tab时前后移动速度不同 有一个弹性效果
            if (initialState < targetState) {
                spring(dampingRatio = 0.5f, stiffness = 200f)
            } else {
                spring(dampingRatio = 0.5f, stiffness = 1000f)
            }
        }, label = "indicator start"
    ) {
        tabPositions[it].left
    }

    val indicatorEnd by transition.animateDp(
        transitionSpec = {
            // 使得指示器在切换tab时前后移动速度不同 有一个弹性效果
            if (initialState < targetState) {
                spring(dampingRatio = 0.5f, stiffness = 1000f)
            } else {
                spring(dampingRatio = 0.5f, stiffness = 200f)
            }
        }, label = "indicator end"
    ) {
        tabPositions[it].right
    }

    Box(
        Modifier
            // Fill up the entire TabRow, and place the indicator at the start
            .wrapContentSize(Alignment.CenterStart)
            // Apply an offset from the start to correctly position the indicator around the tab
            .offset(x = indicatorStart)
            // Make the width of the indicator follow the animated width as we move between tabs
            .width(indicatorEnd - indicatorStart)
            .padding(5.dp)
            .fillMaxHeight((tabPositions[selectedTabIndex].right - tabPositions[selectedTabIndex].left) / (indicatorEnd - indicatorStart))
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .zIndex(-1f) //默认情况会盖住文字
    )
}

@Preview(showBackground = true)
@Composable
private fun TestTabPager() {
    val items = listOf(TabPagerItem("Tab1") {
        Text("Tab1", modifier = Modifier.fillMaxSize())
    }, TabPagerItem("Tab2") {
        Text("Tab2", modifier = Modifier.fillMaxSize())
    })
    TabPager(items)
}