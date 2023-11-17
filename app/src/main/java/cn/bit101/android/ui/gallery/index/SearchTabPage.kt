package cn.bit101.android.ui.gallery.index

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.component.LoadableLazyColumnState
import cn.bit101.android.ui.gallery.common.LoadMoreState
import cn.bit101.android.ui.gallery.common.RefreshState
import cn.bit101.android.ui.gallery.component.PosterCard
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.PostersFilter
import cn.bit101.api.model.common.PostersOrder
import cn.bit101.api.model.http.bit101.GetPostersDataModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTabPage(
    query: String,
    lastSearchQuery: String,
    selectOrder: PostersOrder,
    posters: List<GetPostersDataModel.ResponseItem>,
    state: LoadableLazyColumnState,
    searchState: RefreshState?,
    loadState: LoadMoreState?,

    onQueryChange: (String) -> Unit,
    onSelectOrderChange: (PostersOrder) -> Unit,
    onSearch: (String, PostersOrder, Int) -> Unit,
    onOpenPoster: (Long) -> Unit,
    onOpenImage: (Image) -> Unit,
    onPost: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val orders = PostersOrder.values()

    PostersTabPage(
        header = {
            SearchBar(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .semantics { },
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
                                text = { Text(order.orderName) },
                                onClick = {
                                    onSelectOrderChange(order)
                                    onSearch(query, selectOrder, PostersFilter.PUBLIC_ANONYMOUS)
                                },
                                leadingIcon = {
                                    if(selectOrder == order) {
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
            ) {}
        },
        posters = posters,
        highlightId = lastSearchQuery.toLongOrNull(),

        refreshState = searchState,
        loadState = loadState,
        onRefresh = { onSearch(query, selectOrder, PostersFilter.PUBLIC_ANONYMOUS) },
        state = state,
        onOpenPoster = onOpenPoster,
        onOpenImage = onOpenImage,
        onPost = onPost,
    )
}