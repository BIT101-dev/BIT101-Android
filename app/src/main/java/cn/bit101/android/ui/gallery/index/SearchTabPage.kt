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
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.api.model.common.PostersFilter
import cn.bit101.api.model.common.PostersOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTabPage(
    mainController: MainController,

    /**
     * 查询的数据
     */
    searchData: SearchData,

    /**
     * 帖子列表
     */
    state: PostersState,

    /**
     * 修改查询数据的回调
     */
    onSearchDataChanged: (SearchData) -> Unit,

    /**
     * 执行查询的回调
     */
    onSearch: (SearchData) -> Unit,

    /**
     * 打开帖子
     */
    onOpenPoster: (Long) -> Unit,

    /**
     * 打开发帖或者编辑帖子的界面
     */
    onPost: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val orders = PostersOrder.nameAndValues

    PostersTabPage(
        mainController = mainController,
        header = {

            SearchBar(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .fillMaxWidth(),
                query = searchData.search,
                onQueryChange = { onSearchDataChanged(searchData.copy(search = it)) },
                onSearch = { onSearch(searchData) },
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
                                    val newSearchData = searchData.copy(order = order.value)
                                    onSearchDataChanged(newSearchData)
                                    onSearch(newSearchData)
                                },
                                leadingIcon = {
                                    if(searchData.order == order.value) {
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
        postersState = state,
        onOpenPoster = onOpenPoster,
        onOpenPostOrEdit = onPost,
    )
}