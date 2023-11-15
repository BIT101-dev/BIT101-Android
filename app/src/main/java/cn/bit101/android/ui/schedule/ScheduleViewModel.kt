package cn.bit101.android.ui.schedule

import androidx.lifecycle.ViewModel
import cn.bit101.android.datastore.UserDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
) : ViewModel() {
    val loginStatusFlow = UserDataStore.loginStatus.flow

}