package cn.bit101.android.ui.gallery.index

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.component.LoadableLazyColumnState
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.PostersFilter
import cn.bit101.api.model.common.PostersOrder
import cn.bit101.api.model.http.bit101.GetPostersDataModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTabPage(
    mainController: MainController,
    nestedScrollConnection: NestedScrollConnection? = null,
    navBarHeight: Dp,

    /**
     * 查询的文本
     */
    query: String,

    /**
     * 上一次执行查询操作的查询文本
     */
    lastSearchQuery: String,

    /**
     * 排序
     * @see PostersOrder
     */
    selectOrder: String,

    /**
     * 帖子列表
     */
    posters: List<GetPostersDataModel.ResponseItem>,

    /**
     * 帖子列表的状态，这里既有下拉刷新又有上拉到底部加载更多
     */
    state: LoadableLazyColumnState,

    /**
     * 搜索的状态（也是下拉刷新的状态）
     */
    searchState: SimpleState?,

    /**
     * 加载更多的状态
     */
    loadState: SimpleState?,

    /**
     * 查询文本改变的回调
     */
    onQueryChange: (String) -> Unit,

    /**
     * 排序改变的回调
     */
    onSelectOrderChange: (String) -> Unit,

    /**
     * 执行查询的回调
     */
    onSearch: (String, String, Int) -> Unit,

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
    var expanded by remember { mutableStateOf(false) }

    val orders = PostersOrder.nameAndValues

    PostersTabPage(
        mainController = mainController,
        nestedScrollConnection = nestedScrollConnection,
        header = {

            SearchBar(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                query = query,
                onQueryChange = onQueryChange,
                onSearch = { onSearch(query, selectOrder, PostersFilter.PUBLIC_ANONYMOUS) },
                active = false,
                onActiveChange = {  },
                placeholder = { Text("请输入关键词", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        orders.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.name) },
                                onClick = {
                                    onSelectOrderChange(order.value)
                                    onSearch(query, order.value, PostersFilter.PUBLIC_ANONYMOUS)
                                },
                                leadingIcon = {
                                    if(selectOrder == order.value) {
                                        Icon(
                                            imageVector = Icons.Sharp.Check,
                                            contentDescription = "state"
                                        )
                                    }
                                }
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {}
        },
        posters = posters,
        highlightId = lastSearchQuery.toLongOrNull(),
        navBarHeight = navBarHeight,

        refreshState = searchState,
        loadState = loadState,
        onRefresh = { onSearch(query, selectOrder, PostersFilter.PUBLIC_ANONYMOUS) },
        state = state,
        onOpenImages = onOpenImages,
        onOpenPoster = onOpenPoster,
        onOpenPostOrEdit = onOpenPostOrEdit,
    )
}