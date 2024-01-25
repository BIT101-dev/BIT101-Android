package cn.bit101.android.features.setting.page

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.features.common.component.Avatar
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.SimpleState
import cn.bit101.android.features.common.helper.rememberImagePicker
import cn.bit101.android.features.setting.component.SettingItemData
import cn.bit101.android.features.setting.component.SettingsColumn
import cn.bit101.android.features.setting.component.SettingsGroup
import cn.bit101.android.features.setting.viewmodel.AccountViewModel
import cn.bit101.api.model.common.User


@Composable
private fun AccountPageContent(
    ifLogin: Boolean,
    checkingLogin: Boolean,
    updatingUserInfo: Boolean,
    user: User?,
    sid: String?,
    onOpenEditAvatarDialog: () -> Unit,
    onOpenEditNicknameDialog: () -> Unit,
    onOpenEditMottoDialog: () -> Unit,
    onCheckLogin: () -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
) {
    val infoItems = listOf(
        SettingItemData.Custom(
            title = "头像",
            onClick = onOpenEditAvatarDialog,
            enable = !updatingUserInfo,
            suffix = {
                Avatar(
                    user = user,
                    low = false,
                    showIdentity = false,
                )
            }
        ),
        SettingItemData.Button(
            title = "昵称",
            text = user?.nickname ?: "",
            enable = !updatingUserInfo,
            onClick = onOpenEditNicknameDialog,
        ),
        SettingItemData.Button(
            title = "个性签名",
            text = user?.motto ?: "",
            enable = !updatingUserInfo,
            onClick = onOpenEditMottoDialog,
        ),
        SettingItemData.Card(
            title = "学号",
            text = sid ?: "",
        ),
        SettingItemData.Card(
            title = "UID",
            text = user?.id?.toString() ?: "",
        ),
    )

    val loginItems = listOf(
        SettingItemData.Button(
            title = "登录状态检查",
            subTitle = "登录分为两步，首先进行学校统一身份认证，然后登录BIT101。点击检查二者登录状态",
            onClick = onCheckLogin,
            enable = !checkingLogin,
            text = if (ifLogin) "已登录" else "未登录",
        ),

        if(ifLogin) SettingItemData.Button(
            title = "退出登录",
            onClick = onLogout,
        ) else SettingItemData.Button(
            title = "登录",
            onClick = onLogin,
        ),
    )

    SettingsColumn{
        SettingsGroup(
            title = "个人信息",
            items = infoItems,
            visible = ifLogin && user != null,
        )
        SettingsGroup(
            title = "登录状态",
            items = loginItems,
        )
    }
}

@Composable
private fun EditDialog(
    title: String,
    text: String,
    onTextChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(text = "确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消")
            }
        },
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                shape = RoundedCornerShape(8.dp),
                value = text,
                onValueChange = onTextChanged
            )
        }
    )
}

@Composable
internal fun AccountPage(
    onLogin: () -> Unit,
    onSnackBar: (String) -> Unit,
) {
    val vm: AccountViewModel = hiltViewModel()

    val getUserInfoState by vm.getUserInfoStateFlow.collectAsState()

    val ifLogin by vm.loginStatusFlow.collectAsState(initial = false)

    val checkingLoginState by vm.checkLoginStateFlow.collectAsState()

    val sid by vm.sidFlow.collectAsState(initial = null)

    val updateAvatarState by vm.updateAvatarStateFlow.collectAsState()

    val updateUserInfoState by vm.updateUserInfoStateFlow.collectAsState()


    LaunchedEffect(ifLogin, checkingLoginState) {
        if (ifLogin && (getUserInfoState == null || getUserInfoState is SimpleDataState.Fail) && checkingLoginState !is SimpleState.Loading) {
            vm.getUserInfo()
        }
    }

    LaunchedEffect(checkingLoginState) {
        if (checkingLoginState is SimpleState.Fail) {
            onSnackBar("登录状态检查失败")
        } else if (checkingLoginState is SimpleState.Success) {
            onSnackBar("登录状态检查成功")
        }
    }

    LaunchedEffect(updateAvatarState, updateUserInfoState) {
        if (updateAvatarState is SimpleState.Fail || updateUserInfoState is SimpleState.Fail) {
            onSnackBar("更新失败")
        } else if (updateAvatarState is SimpleState.Success || updateUserInfoState is SimpleState.Success) {
            onSnackBar("更新成功")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            vm.clearStates()
        }
    }

    var showNicknameEditDialog by rememberSaveable { mutableStateOf(false) }
    var showMottoEditDialog by rememberSaveable { mutableStateOf(false) }

    val imagePicker = rememberImagePicker(
        vm::updateAvatar
    )

    val user = (getUserInfoState as? SimpleDataState.Success)?.data?.user

    AccountPageContent(
        user = user,
        sid = sid,
        ifLogin = ifLogin,
        updatingUserInfo = updateAvatarState == SimpleState.Loading || updateUserInfoState == SimpleState.Loading,
        checkingLogin = checkingLoginState == SimpleState.Loading,
        onOpenEditAvatarDialog = imagePicker::pickImage,
        onOpenEditNicknameDialog = { showNicknameEditDialog = true },
        onOpenEditMottoDialog = { showMottoEditDialog = true },
        onCheckLogin = vm::checkLogin,
        onLogout = vm::logout,
        onLogin = onLogin
    )

    if(showNicknameEditDialog && user != null) {
        var editingNickname by rememberSaveable(user.nickname) { mutableStateOf(user.nickname) }
        EditDialog(
            title = "修改昵称",
            text = editingNickname,
            onDismiss = { showNicknameEditDialog = false },
            onTextChanged = { editingNickname = it },
            onConfirm = { vm.updateUserInfo(nickname = editingNickname) }
        )
    }

    if(showMottoEditDialog && user != null) {
        var editingMotto by rememberSaveable(user.motto) { mutableStateOf(user.motto) }
        EditDialog(
            title = "修改个性签名",
            text = editingMotto,
            onDismiss = { showMottoEditDialog = false },
            onTextChanged = { editingMotto = it },
            onConfirm = { vm.updateUserInfo(motto = editingMotto) }
        )
    }
}