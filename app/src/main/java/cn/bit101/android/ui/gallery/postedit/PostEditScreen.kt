package cn.bit101.android.ui.gallery.postedit

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.common.UploadImageData
import cn.bit101.android.ui.component.gallery.DeleteImageDialog
import cn.bit101.android.ui.component.image.UploadImageRow
import cn.bit101.api.model.common.Claim
import cn.bit101.api.model.common.Image


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsContent(
    tags: List<String>,

    onClick: (Int) -> Unit,
    onAddTag: () -> Unit,
) {
    FlowRow {
        tags.forEachIndexed { index, tag ->
            SuggestionChip(
                modifier = Modifier.padding(end = 8.dp),
                shape = CircleShape,
                onClick = { onClick(index) },
                label = { Text(text = tag) }
            )
        }
        IconButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(width = 40.dp, height = 32.dp)
                .padding(end = 8.dp),
            onClick = onAddTag,
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = "add tag")
        }
    }
}

@Composable
fun TagEditDialog(
    tag: String,
    minLength: Int = 1,
    maxLength: Int = 11,

    onTagChange: (String) -> Unit,
    onDelete: () -> Unit,
    onEnsure: (String) -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text(
                    text = "删除",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if(tag.length >= minLength) onEnsure(tag) }
            ) {
                Text(text = "确定")
            }
        },
        text = {
            // 最多11个字
            TextField(
                modifier = Modifier.fillMaxWidth(),
                isError = tag.length < minLength,
                value = tag,
                onValueChange = { onTagChange(it.take(maxLength)) },
                placeholder = { Text(text = "输入标签") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(10.dp),
                supportingText = {
                    Text(text = "${tag.length}/$maxLength")
                }
            )
        },
        title = {
            Text(text = "添加或编辑标签")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimsDropDownBox(
    selected: Claim,
    claims: List<Claim>,

    onSelectIndex: (Claim) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selected.text,
            onValueChange = { expanded = false },
            readOnly = true,
            shape = RoundedCornerShape(10.dp),
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
        )
        ExposedDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = { expanded = !expanded }
        ) {
            claims.forEachIndexed { index, claim ->
                DropdownMenuItem(
                    text = { Text(text = claim.text) },
                    onClick = {
                        onSelectIndex(claims[index])
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PostScreenContentAnonymous(
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text("匿名") },
        shape = CircleShape,
        leadingIcon = {
            if(selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "匿名",
                )
            }
        }
    )
}

@Composable
fun PostScreenContentPublic(
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text("公开") },
        shape = CircleShape,
        leadingIcon = {
            if(selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "公开",
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreenContent(
    mainController: MainController,

    id: Long?,
    /**
     * 数据
     */
    title: String,
    text: String,
    uploadImageData: UploadImageData,
    tags: List<String>,
    claim: Claim,
    anonymous: Boolean,
    public: Boolean,

    /**
     * 获取声明的状态
     */
    claimsState: GetClaimsState?,

    /**
     * 发布的状态
     */
    postState: PutOrPostPosterState?,

    onOpenImage: (Image) -> Unit,
    onUploadImage: () -> Unit,

    /**
     * 修改数据
     */
    onSetTitle: (String) -> Unit,
    onSetText: (String) -> Unit,
    onSelectClaim: (Claim) -> Unit,
    onChangeAnonymous: () -> Unit,
    onChangePublic: () -> Unit,

    onShowEditTagDialog: (Int) -> Unit,

    onPost: () -> Unit,
    onOpenDeleteImageDialog: (Int) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if(id == null) "新建帖子" else "编辑帖子#$id",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { mainController.navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // 标题
            item("title") {
                Text(text = "标题", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.padding(4.dp))
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = title,
                    onValueChange = onSetTitle,
                    singleLine = true,
                    placeholder = { Text(text = "在这里输入标题哦") },
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                )
            }

            item(0) {
                Spacer(modifier = Modifier.padding(8.dp))
            }

            // 内容
            item("text") {
                Text(text = "内容", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.padding(4.dp))
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = text,
                    onValueChange = onSetText,
                    placeholder = { Text(text = "在这里输入内容哦") },
                    shape = RoundedCornerShape(10.dp),
                    minLines = 5,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                )
            }

            item(1) {
                Spacer(Modifier.padding(8.dp))
            }

            // 图片
            item("image") {
                Text(text = "图片", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.padding(4.dp))
                UploadImageRow(
                    images = uploadImageData.images,
                    onUploadImage = onUploadImage,
                    onOpenImage = onOpenImage,
                    onOpenDeleteDialog = onOpenDeleteImageDialog,
                )
            }

            item(2) {
                Spacer(Modifier.padding(8.dp))
            }

            // 标签
            item("tags") {
                Text(text = "标签", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.padding(1.dp))
                Text(text = "请至少添加2个标签，合适的标签将有助于内容推荐。", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.padding(6.dp))
                TagsContent(
                    tags = tags,
                    onClick = { onShowEditTagDialog(it) },
                    onAddTag = { onShowEditTagDialog(tags.size) }
                )
            }

            item(3) {
                Spacer(Modifier.padding(8.dp))
            }
            // 声明
            item("claim") {
                Text(text = "声明", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.padding(1.dp))
                Text(text = "请根据社区公约选择合适的声明，否则可能会被制裁。", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.padding(6.dp))

                when(claimsState) {
                    is GetClaimsState.Loading, null -> {
                        Text(text = "加载中")
                    }
                    is GetClaimsState.Success -> {
                        ClaimsDropDownBox(
                            selected = claim,
                            claims = claimsState.claims,
                            onSelectIndex = onSelectClaim
                        )
                    }
                    is GetClaimsState.Error -> {
                        Text(text = "加载失败")
                    }
                }
            }

            item(23) {
                Spacer(modifier = Modifier.padding(8.dp))
            }

            // 发布按钮
            item("publish") {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onPost,
                            enabled = postState !is PutOrPostPosterState.Loading,
                        ) {
                            if(postState is PutOrPostPosterState.Loading) {
                                CircularProgressIndicator()
                            } else {
                                Row {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Send,
                                        contentDescription = "发布",
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text(
                                        text = "发布",
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart),
                    ) {
                        PostScreenContentAnonymous(
                            selected = anonymous,
                            onClick = onChangeAnonymous,
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        PostScreenContentPublic(
                            selected = public,
                            onClick = onChangePublic,
                        )
                    }
                }
            }

            item(25) {
                Spacer(modifier = Modifier.padding(8.dp))
            }
        }
    }


}


@Composable
fun PostEditScreen(
    mainController: MainController,
    id: Long? = null,
    vm: PostEditViewModel = hiltViewModel(),
) {
    LaunchedEffect(id) {
        vm.loadPoster(id)
    }

    val ctx = LocalContext.current

    val title by vm.titleFlow.collectAsState()
    val text by vm.textFlow.collectAsState()
    val tags by vm.tagsFlow.collectAsState()
    val claim by vm.claimFlow.collectAsState()
    val anonymous by vm.anonymousFlow.collectAsState()
    val public by vm.publicFlow.collectAsState()

    val loadState by vm.loadPosterFlow.collectAsState()

    val uploadImageData by vm.uploadImagesStateFlow.collectAsState()

    val claimsState by vm.getClaimsStateLiveData.observeAsState()

    var deleteImageDialogState by remember { mutableIntStateOf(-1) }

    LaunchedEffect(claimsState) {
        if(claimsState == null) {
            vm.loadClaim()
        }
    }

    var showEditDialog by remember { mutableIntStateOf(-1) }

    val postState by vm.postStateLiveData.observeAsState()

    LaunchedEffect(postState) {
        if(postState is PutOrPostPosterState.Success) {
            if(id == null) {
                mainController.navController.popBackStack()
                mainController.navController.navigate("poster/${(postState as PutOrPostPosterState.Success).id}")
                mainController.snackbar("发布成功OvO")
            } else {
                mainController.navController.popBackStack()
                mainController.navController.popBackStack()
                mainController.navController.navigate("poster/$id")
                mainController.snackbar("修改成功OvO")
            }
        } else if(postState is PutOrPostPosterState.Error) {
            mainController.snackbar("帖子发布或修改失败Orz")
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    vm.uploadImage(ctx, uri)
                }
            }
        }
    }
    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
    }

    if(claimsState is GetClaimsState.Success && loadState is SimpleState.Success) {
        val ensureClaimsState = claimsState as GetClaimsState.Success

        LaunchedEffect(claim) {
            if(claim == null) {
                vm.setClaim(ensureClaimsState.claims[0])
            }
        }

        if(claim != null) PostScreenContent(
            mainController = mainController,
            id = id,
            title = title,
            text = text,
            uploadImageData = uploadImageData,
            tags = tags,
            anonymous = anonymous,
            public = public,
            claim = claim!!,

            claimsState = ensureClaimsState,
            postState = postState,

            onOpenImage = mainController::showImage,
            onSetTitle = { vm.setTitle(it) },
            onSetText = { vm.setText(it) },
            onChangeAnonymous = { vm.setAnonymous(!anonymous) },
            onChangePublic = { vm.setPublic(!public) },
            onShowEditTagDialog = { showEditDialog = it },
            onUploadImage = { imagePickerLauncher.launch(intent) },
            onSelectClaim = vm::setClaim,
            onOpenDeleteImageDialog = { deleteImageDialogState = it },
            onPost = {
                if(title.isEmpty()) {
                    mainController.snackbar("标题不能为空哟")
                } else if(text.isEmpty()) {
                    mainController.snackbar("内容不能为空哟")
                } else if(tags.size < 2) {
                    mainController.snackbar("请至少添加2个标签哟")
                } else if(tags.toSet().size != tags.size) {
                    mainController.snackbar("不要添加重复的标签呦")
                } else {
                    if (id == null) {
                        vm.post(
                            anonymous = anonymous,
                            claim = claim ?: ensureClaimsState.claims[0],
                            uploadImageData = uploadImageData,
                            public = public,
                            tags = tags,
                            text = text,
                            title = title,
                        )
                    } else {
                        vm.put(
                            id = id,
                            anonymous = anonymous,
                            claim = claim ?: ensureClaimsState.claims[0],
                            uploadImageData = uploadImageData,
                            public = public,
                            tags = tags,
                            text = text,
                            title = title,
                        )
                    }
                }
            },
        )


    } else if(claimsState is GetClaimsState.Loading || claimsState == null ||
        loadState is SimpleState.Loading || loadState == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
                .width(64.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
                .width(64.dp)
        ) {
            Text(text = "加载失败")
        }
    }

    if(showEditDialog != -1 && showEditDialog <= tags.size) {
        var tag by remember(showEditDialog) {
            mutableStateOf(if(showEditDialog == tags.size) "" else tags[showEditDialog])
        }

        TagEditDialog(
            tag = tag,
            onTagChange = { tag = it },
            onDelete = {
                vm.setTags(
                    tags.toMutableList().apply {
                        if(showEditDialog != tags.size) removeAt(showEditDialog)
                    }
                )
                showEditDialog = -1
            },
            onEnsure = {
                vm.setTags(
                    tags.toMutableList().apply {
                        if(showEditDialog == tags.size) add(tag)
                        else set(showEditDialog, it)
                    }
                )
                showEditDialog = -1
            },
            onCancel = { showEditDialog = -1 }
        )
    }

    if(deleteImageDialogState != -1) {
        val index = deleteImageDialogState
        DeleteImageDialog(
            onDismiss = { deleteImageDialogState = -1 },
            onConfirm = {
                vm.deleteImage(index)
                deleteImageDialogState = -1
            }
        )
    }
}