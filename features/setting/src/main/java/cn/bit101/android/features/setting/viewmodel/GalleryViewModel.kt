package cn.bit101.android.features.setting.viewmodel

import androidx.lifecycle.ViewModel
import cn.bit101.android.config.setting.base.GallerySettings
import cn.bit101.android.features.common.helper.withScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

internal data class GallerySettingData(
    val hideBotPoster: Boolean,
    val hideBotPosterInSearch: Boolean,
) {
    companion object {
        val default = GallerySettingData(
            hideBotPoster = false,
            hideBotPosterInSearch = false
        )
    }
}

@HiltViewModel
internal class GalleryViewModel @Inject constructor(
    private val gallerySettings: GallerySettings
) : ViewModel() {
    val settingDataFlow = combine(
        gallerySettings.hideBotPoster.flow,
        gallerySettings.hideBotPosterInSearch.flow
    ) { settings ->
        GallerySettingData(
            hideBotPoster = settings[0],
            hideBotPosterInSearch = settings[1],
        )
    }

    fun setSettingData(settingData: GallerySettingData) = withScope {
        gallerySettings.hideBotPoster.set(settingData.hideBotPoster)
        gallerySettings.hideBotPosterInSearch.set(settingData.hideBotPosterInSearch)
    }
}