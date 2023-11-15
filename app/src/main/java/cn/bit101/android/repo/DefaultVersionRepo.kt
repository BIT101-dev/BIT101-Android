package cn.bit101.android.repo

import android.util.Log
import cn.bit101.android.net.APIManager
import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.VersionRepo
import cn.bit101.api.model.http.app.GetVersionDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultVersionRepo @Inject constructor() : VersionRepo {
    override suspend fun getVersionInfo() = withContext(Dispatchers.IO) {
        try {
            BIT101API.app.getVersion().body()
        } catch (e: Exception) {
            Log.e("StatusManager", "getVersionInfo error ${e.message}")
            null
        }
    }
}