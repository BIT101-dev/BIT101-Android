package cn.bit101.android.features.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.GallerySettings
import cn.bit101.android.data.repo.base.UserRepo
import cn.bit101.android.features.common.helper.SimpleDataState
import cn.bit101.android.features.common.helper.withScope
import cn.bit101.android.features.common.helper.withSimpleDataStateLiveData
import cn.bit101.api.model.common.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal data class GallerySettingData(
    val hideBotPoster: Boolean,
    val hideBotPosterInSearch: Boolean,
    val hideStrictMode: Boolean,
    val allowHorizontalScroll: Boolean,
) {
    companion object {
        val default = GallerySettingData(
            hideBotPoster = false,
            hideBotPosterInSearch = false,
            hideStrictMode = false,
            allowHorizontalScroll = false
        )
    }
}

@HiltViewModel
internal class GalleryViewModel @Inject constructor(
    private val gallerySettings: GallerySettings,
    private val userRepo: UserRepo,
) : ViewModel() {
    val settingDataFlow = combine(
        gallerySettings.hideBotPoster.flow,
        gallerySettings.hideBotPosterInSearch.flow,
        gallerySettings.hideStrictMode.flow,
        gallerySettings.allowHorizontalScroll.flow,
    ) { settings ->
        GallerySettingData(
            hideBotPoster = settings[0],
            hideBotPosterInSearch = settings[1],
            hideStrictMode = settings[2],
            allowHorizontalScroll = settings[3],
        )
    }

    val hideUserUidsFlow = gallerySettings.hideUserUids.flow

    val hideUserInfosFlow = MutableLiveData<SimpleDataState<List<User>>?>(null)

    fun getHideUserInfos() = withSimpleDataStateLiveData(hideUserInfosFlow) {
        hideUserUidsFlow.map { uids ->
            uids.map { uid ->
                userRepo.getUserInfo(uid.toLong()).user
            }
        }.first()
    }

    fun reshowUser(index: Int) {
        val infoList = (hideUserInfosFlow.value as? SimpleDataState.Success)?.data?.toMutableList()
        withSimpleDataStateLiveData(hideUserInfosFlow) {
            val uidList = gallerySettings.hideUserUids.get().toMutableList()
            uidList.removeAt(index)
            gallerySettings.hideUserUids.set(uidList)

            infoList!!.removeAt(index)
            infoList
        }
    }

    fun switchShowAnonymous() = withScope {
        val uidList = gallerySettings.hideUserUids.get()
        if (uidList.isNotEmpty() && uidList.first() == -1) {
            gallerySettings.hideUserUids.set(uidList.drop(1))
        } else {
            gallerySettings.hideUserUids.set(listOf(-1).plus(uidList))
        }
    }

    fun setSettingData(settingData: GallerySettingData) = withScope {
        gallerySettings.hideBotPoster.set(settingData.hideBotPoster)
        gallerySettings.hideBotPosterInSearch.set(settingData.hideBotPosterInSearch)
        gallerySettings.hideStrictMode.set(settingData.hideStrictMode)
        gallerySettings.allowHorizontalScroll.set(settingData.allowHorizontalScroll)
    }
}