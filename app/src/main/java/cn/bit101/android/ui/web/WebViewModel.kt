package cn.bit101.android.ui.web

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import cn.bit101.android.App
import cn.bit101.android.datastore.UserDataStore
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.AccompanistWebViewClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@HiltViewModel
class WebViewModel @Inject constructor(

) : ViewModel() {
    val BASE_URL = "https://bit101.cn"
}