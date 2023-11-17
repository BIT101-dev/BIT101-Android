package cn.bit101.android.ui.gallery.user

import androidx.compose.runtime.Composable
import cn.bit101.android.ui.MainController
import cn.bit101.api.model.common.Avatar
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel

@Composable
fun EditUserDialog(
    data: GetUserInfoDataModel.Response,
    onDismiss: () -> Unit,
    onSave: (Avatar, String, String) -> Unit,
) {

}


@Composable
fun FollowerDialog(
    mainController: MainController,
    data: GetUserInfoDataModel.Response,
    onDismiss: () -> Unit,
) {

}


@Composable
fun FollowingDialog(
    mainController: MainController,
    data: GetUserInfoDataModel.Response,
    onDismiss: () -> Unit,
) {

}