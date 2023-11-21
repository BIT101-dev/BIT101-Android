package cn.bit101.android.ui.component.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.gallery.poster.CommentEditData
import cn.bit101.api.model.common.Image


@Composable
fun CommentEditContentTextField(
    value: String,
    maxLines: Int = 1000,
    onValueChange: (String) -> Unit,
) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        shape = RoundedCornerShape(15.dp),
        placeholder = { Text(text = "输入评论吧") },
        minLines = 3,
        maxLines = maxLines,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
fun CommentEditContentUploadImageChip(
    selected: Boolean,
    onClick: () -> Unit,
) {
    // 是否附图
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text("附图") },
        shape = CircleShape,
        leadingIcon = {
            if(selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "附图",
                )
            }
        }
    )
}


@Composable
fun CommentEditContentAnonymous(
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
fun CommentEditContent(
    /**
     * TextField最大行数
     */
    textFieldMaxLines: Int = 1000,

    /**
     * 评论编辑数据
     */
    commentEditData: CommentEditData,

    /**
     * 是否正在发送
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
    onSendComment: (CommentEditData) -> Unit,

    /**
     * 打开删除图片对话框
     */
    onOpenDeleteImageDialog: (Int) -> Unit,
) {
    Column {
        CommentEditContentTextField(
            value = commentEditData.text,
            maxLines = textFieldMaxLines,
            onValueChange = { value ->
                onEditComment(commentEditData.copy(text = value))
            },
        )
        Spacer(modifier = Modifier.padding(4.dp))

        if(commentEditData.uploadImageData.ifUpdate) {
            Spacer(modifier = Modifier.padding(4.dp))
            UploadImageRow(
                images = commentEditData.uploadImageData.images,
                onUploadImage = onUploadImage,
                onOpenImage = onOpenImage,
                onOpenDeleteDialog = onOpenDeleteImageDialog,
            )
            Spacer(modifier = Modifier.padding(4.dp))
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Row {
                CommentEditContentUploadImageChip(
                    selected = commentEditData.uploadImageData.ifUpdate,
                    onClick = {
                        onEditComment(commentEditData.copy(
                            uploadImageData = commentEditData.uploadImageData.copy(
                                ifUpdate = !commentEditData.uploadImageData.ifUpdate
                            )
                        ))
                    },
                )
                Spacer(modifier = Modifier.padding(4.dp))

                // 是否匿名
                CommentEditContentAnonymous(
                    selected = commentEditData.anonymous,
                    onClick = {
                        onEditComment(commentEditData.copy(anonymous = !commentEditData.anonymous))
                    },
                )
            }

            Button(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = {
                    onSendComment(commentEditData)
                },
                enabled = !sending
            ) {
                if (sending) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                ) else Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.Send, contentDescription = "")
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "发送")
                }
            }
        }
    }
}