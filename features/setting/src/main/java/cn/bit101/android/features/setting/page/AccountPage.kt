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
import cn.bit101.android.data.repo.base.CommunityState
import cn.bit101.android.features.common.component.Avatar
import cn.bit101.android.features.common.helper.SimpleErrorDataState
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
    schoolConnected: Boolean,
    community: CommunityState,
    onRetryCommunity: () -> Unit,
    checkingLogin: Boolean,
    updatingUserInfo: Boolean,
    user: User?,
    sid: String?,
    onOpenEditAvatarDialog: () -> Unit,
    onOpenEditNicknameDialog: () -> Unit,
    onOpenEditMottoDialog: () -> Unit,
    onReconnectSchool: () -> Unit,
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

    val loginItems = buildList {
        add(SettingItemData.Button(
            title = "校园账号",
            subTitle = "校园身份与教务会话独立，具体功能会按需连接学校服务。点击重新验证校园账号。",
            onClick = onReconnectSchool,
            enable = !checkingLogin && !sid.isNullOrBlank(),
            text = if (schoolConnected) "身份已验证" else "待验证",
        ))
        add(SettingItemData.Card(title = "BIT101", text = community.message))
        if (!community.connected) {
            add(SettingItemData.Button(title = "立即重试 BIT101", enable = !community.connecting, onClick = onRetryCommunity))
        }
        add(SettingItemData.Button(title = "手动登录或切换账号", subTitle = "选择校园账号或 BIT101 登录", onClick = onLogin))
        if (!sid.isNullOrBlank()) add(SettingItemData.Button(
            title = "退出登录", subTitle = "清除校园和 BIT101 登录状态及保存的账号密码", onClick = onLogout,
        ))
    }

    SettingsColumn{
        SettingsGroup(
            title = "个人信息",
            items = infoItems,
            visible = community.connected && user != null,
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

    val schoolConnected by vm.schoolConnected.collectAsState(initial = false)
    val community by vm.communityState.collectAsState(initial = CommunityState())

    val checkingLoginState by vm.reconnectSchoolStateFlow.collectAsState()

    val sid by vm.sidFlow.collectAsState(initial = null)

    val updateAvatarState by vm.updateAvatarStateFlow.collectAsState()

    val updateUserInfoState by vm.updateUserInfoStateFlow.collectAsState()


    LaunchedEffect(community.connected, sid) {
        if (community.connected) vm.getUserInfo() else vm.clearUserInfo()
    }

    LaunchedEffect(checkingLoginState) {
        when (val result = checkingLoginState) {
            is SimpleErrorDataState.Fail -> onSnackBar(result.msg)
            is SimpleErrorDataState.Success -> onSnackBar(result.data)
            else -> Unit
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
        schoolConnected = schoolConnected,
        community = community,
        onRetryCommunity = vm::retryCommunity,
        updatingUserInfo = updateAvatarState == SimpleState.Loading || updateUserInfoState == SimpleState.Loading,
        checkingLogin = checkingLoginState is SimpleErrorDataState.Loading,
        onOpenEditAvatarDialog = imagePicker::pickImage,
        onOpenEditNicknameDialog = { showNicknameEditDialog = true },
        onOpenEditMottoDialog = { showMottoEditDialog = true },
        onReconnectSchool = vm::reconnectSchool,
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
