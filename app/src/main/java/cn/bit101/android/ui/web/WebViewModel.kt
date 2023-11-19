package cn.bit101.android.ui.web

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class WebViewModel @Inject constructor(

) : ViewModel() {
    val BASE_URL = "https://bit101.cn"
}