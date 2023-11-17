package cn.bit101.android.ui.gallery.poster

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cn.bit101.android.ui.gallery.component.CommentEditContent
import cn.bit101.api.model.common.Comment
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendCommentDialog(
    replyUser: User,
    commentEditData: CommentEditData,
    sending: Boolean,

    onEditComment: (CommentEditData) -> Unit,
    onOpenImage: (Image) -> Unit,
    onUploadImage: () -> Unit,
    onSendComment: (CommentEditData) -> Unit,
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
                    text = "回复给@" + replyUser.nickname + "："
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