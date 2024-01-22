package cn.bit101.android.features.gallery.index

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.MainController
import cn.bit101.android.features.component.common.CustomOutlinedTextField
import cn.bit101.api.model.common.PostersOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    mainController: MainController,

    /**
     * 帖子列表
     */
    state: PostersState,

    searchData: SearchData,

    /**
     * 执行查询的回调
     */
    onSearch: (SearchData) -> Unit,

    onSearchDataChanged: (SearchData) -> Unit,

    /**
     * 打开帖子
     */
    onOpenPoster: (Long) -> Unit,

    /**
     * 打开发帖或者编辑帖子的界面
     */
    onPost: () -> Unit,

    onDismiss: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val orders = PostersOrder.nameAndValues

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = topAppBarScrollBehavior,
                title = {
                    CustomOutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 0.dp, end = 12.dp, top = 0.dp, bottom = 0.dp)
                            .height(42.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        suffix = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Outlined.Reorder, contentDescription = "Localized description")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                orders.forEach {
                                    DropdownMenuItem(
                                        text = { Text(it.name) },
                                        onClick = {
                                            onSearchDataChanged(searchData.copy(order = it.value))
                                            onSearch(searchData.copy(order = it.value))
                                        },
                                        leadingIcon = {
                                            if(searchData.order == it.value) {
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
                        singleLine = true,
                        value = searchData.search,
                        shape = CircleShape,
                        contentPadding = PaddingValues(start = 12.dp, end = 0.dp, top = 0.dp, bottom = 0.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions { onSearch(searchData) },
                        onValueChange = { onSearchDataChanged(searchData.copy(search = it)) },
                        placeholder = { Text(text = "在这里搜索哦", style = MaterialTheme.typography.bodyMedium) }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "关闭",
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            PostersTabPage(
                mainController = mainController,
                postersState = state,
                showPostButton = false,
                onOpenPoster = onOpenPoster,
                onOpenPostOrEdit = onPost,
            )
        }
    }
}