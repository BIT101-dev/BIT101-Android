package cn.bit101.android.repo

import android.util.Log
import cn.bit101.android.net.base.APIManager
import cn.bit101.android.repo.base.VersionRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultVersionRepo @Inject constructor(
    private val apiManager: APIManager
) : VersionRepo {

    private val api = apiManager.api

    override suspend fun getVersionInfo() = withContext(Dispatchers.IO) {
        try {
            api.app.getVersion().body()
        } catch (e: Exception) {
            Log.e("StatusManager", "getVersionInfo error ${e.message}")
            null
        }
    }
}