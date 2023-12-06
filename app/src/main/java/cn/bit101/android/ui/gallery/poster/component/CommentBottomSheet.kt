package cn.bit101.android.ui.gallery.poster.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.NoAccounts
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import cn.bit101.android.ui.common.keyboardAsState
import cn.bit101.android.ui.component.bottomsheet.BottomSheet
import cn.bit101.android.ui.component.bottomsheet.BottomSheetDefaults
import cn.bit101.android.ui.component.bottomsheet.BottomSheetValue
import cn.bit101.android.ui.component.bottomsheet.DialogSheetBehaviors
import cn.bit101.android.ui.component.bottomsheet.rememberBottomSheetState
import cn.bit101.android.ui.component.gallery.UploadImageRow
import cn.bit101.android.ui.gallery.poster.CommentEditData
import cn.bit101.android.ui.gallery.poster.CommentType
import cn.bit101.android.utils.ColorUtils
import cn.bit101.api.model.common.Image
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CommentBottomSheet(
    commentType: CommentType,

    /**
     * 评论的数据
     */
    commentEditData: CommentEditData,

    /**
     * 是否正在发送评论
     */
    sending: Boolean,

    /**
     * 编辑评论
     */
    onEditComment: (CommentType, CommentEditData) -> Unit,

    /**
     * 打开图片
     */
    onOpenImage: (Image) -> Unit,

    /**
     * 上传图片
     */
    onUploadImage: () -> Unit,

    /**
     * 发送评论
     */
    onSendComment: (CommentType, CommentEditData) -> Unit,

    /**
     * 打开删除图片的对话框
     */
    onOpenDeleteImageDialog: (Int) -> Unit,


    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }

    val kc = LocalSoftwareKeyboardController.current

    val isKeyboardOpen by keyboardAsState()

    if(!isKeyboardOpen && focused) {
        onDismiss()
    }

    val scope = rememberCoroutineScope()

    BottomSheet(
        state = rememberBottomSheetState(
            initialValue = BottomSheetValue.Expanded,
            confirmValueChange = {
                if (it == BottomSheetValue.Collapsed) {
                    onDismiss()
                    false
                } else true
            },
        ),
        skipPeeked = true,
        showAboveKeyboard = true,
        allowNestedScroll = false,
        behaviors = DialogSheetBehaviors(
            navigationBarColor = BottomSheetDefaults.backgroundColor,
            lightNavigationBar = ColorUtils.isLightColor(BottomSheetDefaults.backgroundColor),
        ),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        dragHandle = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onPlaced {
                        scope.launch {
                            focusRequester.requestFocus()
                            delay(200)
                            focused = true
                        }
                    }
                    .clickable {
                        scope.launch {
                            focusRequester.requestFocus()
                            delay(200)
                            focused = true
                            kc?.show()
                        }
                    },
                value = commentEditData.text,
                onValueChange = { onEditComment(commentType, commentEditData.copy(text = it)) },
                minLines = 3,
                maxLines = 10,
                placeholder = { Text(text = "输入评论吧") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                )
            )

            UploadImageRow(
                images = commentEditData.uploadImageData.images,
                onUploadImage = onUploadImage,
                onOpenImage = onOpenImage,
                onOpenDeleteDialog = onOpenDeleteImageDialog,
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            focused = false
                            focusRequester.freeFocus()
                            onUploadImage()
                        }
                    ) {
                        Icon(imageVector = Icons.Rounded.Image, contentDescription = "上传图片")
                    }
                }
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { onEditComment(commentType, commentEditData.copy(anonymous = !commentEditData.anonymous)) }) {
                        Icon(
                            imageVector = if(commentEditData.anonymous) Icons.Rounded.NoAccounts
                            else Icons.Rounded.AccountCircle,
                            contentDescription = "匿名"
                        )
                    }
                }
            }


        }
    }
}