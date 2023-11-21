package cn.bit101.android.ui

import androidx.lifecycle.ViewModel
import cn.bit101.android.datastore.SettingDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(

) : ViewModel() {
    val enableGalleryFlow = SettingDataStore.settingEnableGallery.flow




}