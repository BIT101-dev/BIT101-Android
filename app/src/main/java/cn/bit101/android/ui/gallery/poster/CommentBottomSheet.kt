package cn.bit101.android.ui.gallery.poster

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.component.bottomsheet.BottomSheet
import cn.bit101.android.ui.component.bottomsheet.BottomSheetDefaults
import cn.bit101.android.ui.component.bottomsheet.BottomSheetValue
import cn.bit101.android.ui.component.bottomsheet.DialogSheetBehaviors
import cn.bit101.android.ui.component.bottomsheet.rememberBottomSheetState
import cn.bit101.android.utils.ColorUtils
import cn.bit101.api.model.common.Image

@Composable
fun CommentBottomSheet(
    commentType: CommentType,

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


    onDismiss: () -> Unit,
) {
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
        allowNestedScroll = false,
        behaviors = DialogSheetBehaviors(
            navigationBarColor = BottomSheetDefaults.backgroundColor,
            lightNavigationBar = ColorUtils.isLightColor(BottomSheetDefaults.backgroundColor),
        ),
        dragHandle = {},
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .fillMaxWidth()
        ) {

        }
    }
}