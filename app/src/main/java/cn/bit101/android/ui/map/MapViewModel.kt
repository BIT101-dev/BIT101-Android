package cn.bit101.android.ui.map

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.App
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.manager.base.MapSettingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.enableRotation
import ovh.plrapps.mapcompose.api.scrollTo
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapSettingManager: MapSettingManager
) : ViewModel() {
    private val TAG = "MapViewModel"

    // 缓存配置
    private val cacheTime = 7 * 24 * 60 * 60
    private val cacheFile = File(App.context.externalCacheDir, "map_cache")
    private val cacheSize = 10 * 1024 * 1024L
    private val cache = Cache(cacheFile, cacheSize)

    // 地图数据 瓦片服务配置
    private val httpClient = OkHttpClient.Builder()
        .cache(cache)
        .build()
    private val tileStreamProvider = TileStreamProvider { row, col, zoomLvl ->
        val url = "https://map.bit101.flwfdd.xyz/tile/$zoomLvl/$col/$row.png"
        try {
            val request = Request.Builder().url(url)
                .cacheControl(CacheControl.Builder().maxAge(cacheTime, TimeUnit.SECONDS).build())
                .addHeader("User-Agent", "BIT101") //防止被拦截
                .build()
            val response = httpClient.newCall(request).execute()
            response.body?.byteStream()?.buffered()
        } catch (e: Exception) {
            Log.i(TAG, "Get tile error: $e $url")
            null
        }
    }

    // 地图状态
    val state by mutableStateOf(
        MapState(19, 67108864, 67108864).apply {
            addLayer(tileStreamProvider)
            enableRotation()
        })

    // 预定义位置
    data class Position(val x: Double, val y: Double, val scale: Float)

    val LiangXiang = Position(0.822685, 0.37956, 0.25f) //良乡校区
    val ZhongGuanCun = Position(0.823083, 0.37873, 0.25f) //中关村校区

    fun scrollTo(position: Position) {
        viewModelScope.launch {
            state.scrollTo(position.x, position.y, position.scale)
        }
    }

    // 地图缩放倍率
    val mapScaleFlow = mapSettingManager.scale.flow
    fun setMapScale(scale: Float) {
        viewModelScope.launch {
            mapSettingManager.scale.set(scale)
        }
    }

    init {
        scrollTo(LiangXiang)
    }
}