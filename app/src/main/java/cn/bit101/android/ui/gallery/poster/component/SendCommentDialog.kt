package cn.bit101.android.ui.gallery.poster.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.gallery.CommentEditContent
import cn.bit101.android.ui.gallery.poster.CommentEditData
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.User

@Composable
fun SendCommentDialog(
    mainController: MainController,

    /**
     * 要回复的用户
     */
    replyUser: User,

    /**
     * 评论的编辑数据
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
    onSendComment: (CommentEditData) -> Unit,

    /**
     * 打开删除图片的对话框
     */
    onOpenDeleteImageDialog: (Int) -> Unit,

    onClose: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.fillMaxWidth(0.9f),
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = MaterialTheme.typography.bodyLarge.toSpanStyle(),
                        ) {
                            append("回复给")
                        }
                        withStyle(
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.tertiary
                            ).toSpanStyle(),
                        ) {
                            append(" @" + replyUser.nickname + " ")
                        }
                        withStyle(
                            style = MaterialTheme.typography.bodyLarge.toSpanStyle(),
                        ) {
                            append("：")
                        }
                    }
                )
                Spacer(modifier = Modifier.padding(6.dp))
                CommentEditContent(
                    commentEditData = commentEditData,
                    sending = sending,
                    onEditComment = onEditComment,
                    onOpenImage = onOpenImage,
                    onUploadImage = onUploadImage,
                    onSendComment = onSendComment,
                    onOpenDeleteImageDialog = onOpenDeleteImageDialog,
                )
            }
        },
        title = {
            Text(
                text = "回复评论",
                style = MaterialTheme.typography.titleLarge
            )
        },
        dismissButton = {},
        confirmButton = {},
    )
}