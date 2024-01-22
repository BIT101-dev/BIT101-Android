package cn.bit101.android.features.gallery.poster.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.FaceRetouchingOff
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.utils.ColorUtils
import cn.bit101.android.features.component.bottomsheet.BottomSheet
import cn.bit101.android.features.component.bottomsheet.BottomSheetDefaults
import cn.bit101.android.features.component.bottomsheet.BottomSheetValue
import cn.bit101.android.features.component.bottomsheet.DialogSheetBehaviors
import cn.bit101.android.features.component.bottomsheet.rememberBottomSheetState
import cn.bit101.android.features.component.common.EditRowIconButton
import cn.bit101.android.features.component.image.UploadImageRow
import cn.bit101.android.features.gallery.poster.CommentEditData
import cn.bit101.android.features.gallery.poster.CommentType
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
    onEditComment: (CommentEditData) -> Unit,

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
    onSendComment: () -> Unit,

    /**
     * 打开删除图片的对话框
     */
    onOpenDeleteImageDialog: (Int) -> Unit,

    /**
     * 删除上传失败的图片
     */
    onDeleteFailImage: (Int) -> Unit,

    /**
     * 关闭评论框
     */
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var placed by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val state = rememberBottomSheetState(
        initialValue = BottomSheetValue.Expanded,
        confirmValueChange = {
            if (it == BottomSheetValue.Collapsed) {
                onDismiss()
            }
            true
        },
    )


    BottomSheet(
        state = state,
        skipPeeked = true,
        showAboveKeyboard = true,
        allowNestedScroll = false,
        behaviors = DialogSheetBehaviors(
            navigationBarColor = BottomSheetDefaults.backgroundColor,
            lightNavigationBar = ColorUtils.isLightColor(BottomSheetDefaults.backgroundColor),
        ),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp, start = 12.dp, end = 12.dp),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "评论/回复",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    visible = commentEditData.anonymous,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally(),
                ) {
                    Text(
                        text = "匿名",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onPlaced {
                        scope.launch {
                            if (!placed) {
                                focusRequester.requestFocus()
                                delay(100)
                                placed = true
                            }
                        }
                    },
                value = commentEditData.text,
                onValueChange = { onEditComment(commentEditData.copy(text = it)) },
                minLines = 3,
                maxLines = 10,
                placeholder = {
                    val text = when(commentType) {
                        is CommentType.ToComment -> "回复@${commentType.subComment.user.nickname}:"
                        is CommentType.ToPoster -> "评论帖子"
                    }
                    Text(text = text)
                },
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
                ),
                shape = RectangleShape
            )

            if(commentEditData.uploadImageData.ifUpload) {
                Box(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 4.dp)) {
                    UploadImageRow(
                        images = commentEditData.uploadImageData.images,
                        onOpenImage = onOpenImage,
                        onOpenDeleteDialog = onOpenDeleteImageDialog,
                        onDeleteFailImage = onDeleteFailImage
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    EditRowIconButton(
                        icon = if(commentEditData.anonymous) Icons.Outlined.FaceRetouchingOff
                        else Icons.Outlined.Face,
                        onClick = { onEditComment(commentEditData.copy(anonymous = !commentEditData.anonymous)) }
                    )

                    EditRowIconButton(
                        icon = Icons.Outlined.Image,
                        onClick = onUploadImage
                    )
                }
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalButton(
                        onClick = onSendComment,
                        enabled = !sending && !commentEditData.isEmpty(),
                    ) {
                        if(sending) {
                            Text(text = "发送中")
                        } else {
                            Text(text = "发送")
                        }
                    }
                }
            }
        }
    }
}