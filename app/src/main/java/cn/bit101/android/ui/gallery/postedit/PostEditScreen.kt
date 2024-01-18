package cn.bit101.android.ui.gallery.postedit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.FaceRetouchingOff
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.PublicOff
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleDataState
import cn.bit101.android.ui.common.SimpleState
import cn.bit101.android.ui.common.UploadImageState
import cn.bit101.android.ui.common.keyboardStateAsState
import cn.bit101.android.ui.common.rememberImagePicker
import cn.bit101.android.ui.component.common.CircularProgressIndicatorForPage
import cn.bit101.android.ui.component.common.CustomOutlinedTextField
import cn.bit101.android.ui.component.common.EditRowIconButton
import cn.bit101.android.ui.component.common.ErrorMessageForPage
import cn.bit101.android.ui.component.gallery.DeleteImageDialog
import cn.bit101.android.ui.component.image.UploadImageRow
import cn.bit101.api.model.common.Claim
import cn.bit101.api.model.common.Image


@Composable
private fun TagEditDialog(
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
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                isError = tag.length < minLength,
                value = tag,
                onValueChange = { onTagChange(it.take(maxLength)) },
                placeholder = { Text(text = "输入标签") },
                singleLine = true,
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

@Composable
private fun SelectClaimDialog(
    claim: Claim,
    claims: List<Claim>,
    onSelectClaim: (Claim) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedClaim = claims.indexOfFirst { it.id == claim.id }.takeIf { it != -1 } ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onSelectClaim(claim)
                    onDismiss()
                }
            ) {
                Text(text = "确定")
            }
        },
        title = { Text(text = "创作者声明") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                claims.forEachIndexed { idx, claim ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .selectable(
                                selected = (selectedClaim == idx),
                                onClick = {
                                    onSelectClaim(claim)
                                    onDismiss()
                                },
                                role = Role.RadioButton
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedClaim == idx),
                            onClick = null
                        )
                        Text(
                            text = claim.text,
                            modifier = Modifier.padding(start = 10.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreenContent(
    mainController: MainController,

    id: Long?,
    postOrPutting: Boolean,
    editData: EditPosterData,

    onOpenImage: (Image) -> Unit,
    onUploadImage: () -> Unit,
    onEditDataChanged: (EditPosterData) -> Unit,
    onShowSelectClaimDialog: () -> Unit,
    onShowEditTagDialog: (Int) -> Unit,
    onPost: () -> Unit,
    onDeleteImage: (Int) -> Unit,
    onDeleteFailImage: (Int) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val titleFocusRequester = remember { FocusRequester() }
    val textFocusRequester = remember { FocusRequester() }

    val imeStates = keyboardStateAsState()

    val imeHeight by imeStates.first
    val imeVisible by imeStates.second

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = imeHeight),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if(id == null) "发布帖子" else "编辑帖子#$id",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { mainController.navController.popBackStack() }) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedVisibility(visible = editData.anonymous) {
                            Text(
                                text = "匿名",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedVisibility(visible = !editData.public) {
                            Text(
                                text = "仅自己可见",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            CustomOutlinedTextField(
                modifier = Modifier
                    .padding(0.dp)
                    .focusRequester(titleFocusRequester)
                    .fillMaxWidth(),
                value = editData.title,
                onValueChange = { onEditDataChanged(editData.copy(title = it)) },
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                transparent = true,
                contentPadding = PaddingValues(vertical = 0.dp, horizontal = 12.dp),
                keyboardActions = KeyboardActions(
                    onNext = { textFocusRequester.requestFocus() },
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                minLines = 1,
                placeholder = {
                    Text(
                        text = "在这里输入标题",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        ),
                    )
                }
            )

            AnimatedVisibility(visible = (editData.claim?.id != 0 && editData.claim != null)) {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    Spacer(modifier = Modifier.padding(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "创作者声明：${editData.claim?.text ?: ""}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            ),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(6.dp))

            CustomOutlinedTextField(
                modifier = Modifier
                    .padding(0.dp)
                    .weight(1f)
                    .focusRequester(textFocusRequester)
                    .fillMaxWidth(),
                value = editData.text,
                onValueChange = { onEditDataChanged(editData.copy(text = it)) },
                transparent = true,
                contentPadding = PaddingValues(vertical = 0.dp, horizontal = 12.dp),
                placeholder = { Text(text = "在这里输入内容") }
            )

            Spacer(modifier = Modifier.padding(6.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    if(editData.uploadImageData.images.isNotEmpty()) {
                        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            UploadImageRow(
                                images = editData.uploadImageData.images,
                                onOpenDeleteDialog = onDeleteImage,
                                onOpenImage = onOpenImage,
                                onDeleteFailImage = onDeleteFailImage,
                            )
                        }
                    }
                    
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(editData.tags) { idx, tag ->
                            InputChip(
                                shape = CircleShape,
                                onClick = { onShowEditTagDialog(idx) },
                                label = { Text(text = "#$tag") },
                                selected = false,
                            )
                        }

                        if(editData.tags.isEmpty()) {
                            item {
                                InputChip(
                                    shape = CircleShape,
                                    enabled = false,
                                    onClick = {},
                                    label = { Text(text = "#添加一个标签吧") },
                                    selected = false,
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.CenterStart),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            EditRowIconButton(
                                icon = if(editData.anonymous) Icons.Outlined.FaceRetouchingOff
                                else Icons.Outlined.Face,
                                onClick = { onEditDataChanged(editData.copy(anonymous = !editData.anonymous)) },
                            )

                            EditRowIconButton(
                                icon = if(editData.public) Icons.Outlined.Public else Icons.Outlined.PublicOff,
                                onClick = { onEditDataChanged(editData.copy(public = !editData.public)) },
                            )

                            EditRowIconButton(
                                icon = Icons.Outlined.Numbers,
                                onClick = { onShowEditTagDialog(editData.tags.size) },
                            )

                            EditRowIconButton(
                                icon = Icons.Outlined.Image,
                                onClick = onUploadImage,
                            )

                            EditRowIconButton(
                                icon = Icons.Outlined.WarningAmber,
                                onClick = onShowSelectClaimDialog,
                            )
                        }
                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FilledTonalButton(
                                onClick = onPost,
                                enabled = !postOrPutting && !editData.isEmpty(),
                            ) {
                                if(postOrPutting) Text(text = if(id == null) "发布中" else "提交修改中")
                                else Text(text = if(id == null) "发布" else "提交修改")
                            }
                        }
                    }
                }
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

    val editData by vm.editPosterDataFlow.collectAsState()

    val loadPosterState by vm.loadPosterFlow.collectAsState()

    val claimsState by vm.getClaimsStateLiveData.observeAsState()

    var deleteImageDialogState by remember { mutableIntStateOf(-1) }

    var showEditDialog by remember { mutableIntStateOf(-1) }

    var showSelectClaimDialog by remember { mutableStateOf(false) }


    LaunchedEffect(claimsState) {
        if(claimsState == null) {
            vm.loadClaim()
        }
    }


    val postState by vm.postStateLiveData.observeAsState()

    LaunchedEffect(postState) {
        if(postState is SimpleDataState.Success) {
            if(id == null) {
                mainController.navController.popBackStack()
                mainController.navController.navigate("poster/${(postState as SimpleDataState.Success).data}")
                mainController.snackbar("发布成功OvO")
            } else {
                mainController.navController.popBackStack()
                mainController.navController.popBackStack()
                mainController.navController.navigate("poster/$id")
                mainController.snackbar("修改成功OvO")
            }
        } else if(postState is SimpleDataState.Fail) {
            mainController.snackbar("帖子发布或修改失败Orz")
        }
    }

    val imagePicker = rememberImagePicker {
        vm.uploadImage(ctx, it)
    }

    if(claimsState is SimpleDataState.Success && loadPosterState is SimpleState.Success) {
        val claims = (claimsState as SimpleDataState.Success).data

        if(editData.claim == null) {
            vm.setEditData(editData.copy(claim = claims[0]))
        }

        PostScreenContent(
            mainController = mainController,
            id = id,
            editData = editData,
            postOrPutting = postState is SimpleDataState.Loading,
            onOpenImage = mainController::showImage,
            onEditDataChanged = vm::setEditData,
            onShowEditTagDialog = { showEditDialog = it },
            onUploadImage = imagePicker::pickImage,
            onDeleteImage = { deleteImageDialogState = it },
            onShowSelectClaimDialog = { showSelectClaimDialog = true },
            onDeleteFailImage = {
                if(editData.uploadImageData.images.getOrNull(it)?.uploadImageState is UploadImageState.Fail) {
                    vm.deleteImage(it)
                }
            },
            onPost = {
                if(editData.title.isEmpty()) {
                    mainController.snackbar("标题不能为空哟")
                } else if(editData.text.isEmpty()) {
                    mainController.snackbar("内容不能为空哟")
                } else if(editData.tags.size < 2) {
                    mainController.snackbar("请至少添加2个标签哟")
                } else if(editData.tags.toSet().size != editData.tags.size) {
                    mainController.snackbar("不要添加重复的标签呦")
                } else {
                    if (id == null) vm.post()
                    else vm.put(id)
                }
            },
        )
    } else if(claimsState is SimpleDataState.Loading || claimsState == null ||
        loadPosterState is SimpleState.Loading || loadPosterState == null) {
        CircularProgressIndicatorForPage()
    } else {
        ErrorMessageForPage()
    }

    if(showEditDialog != -1 && showEditDialog <= editData.tags.size) {
        var tag by remember(showEditDialog) {
            mutableStateOf(if(showEditDialog == editData.tags.size) "" else editData.tags[showEditDialog])
        }

        TagEditDialog(
            tag = tag,
            onTagChange = { tag = it },
            onDelete = {
                if(showEditDialog == editData.tags.size) return@TagEditDialog
                vm.setEditData(editData.copy(
                    tags = editData.tags.toMutableList().apply { removeAt(showEditDialog) }
                ))
                showEditDialog = -1
            },
            onEnsure = {
                vm.setEditData(editData.copy(
                    tags = editData.tags.toMutableList().apply {
                        if(showEditDialog == editData.tags.size) add(tag)
                        else set(showEditDialog, it)
                    }
                ))
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

    if(showSelectClaimDialog && editData.claim != null && claimsState is SimpleDataState.Success) {
        SelectClaimDialog(
            claim = editData.claim!!,
            claims = (claimsState as SimpleDataState.Success).data,
            onSelectClaim = { vm.setEditData(editData.copy(claim = it)) },
            onDismiss = { showSelectClaimDialog = false }
        )
    }
}