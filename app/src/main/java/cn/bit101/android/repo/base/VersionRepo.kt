package cn.bit101.android.repo.base

import cn.bit101.api.model.http.app.GetVersionDataModel

interface VersionRepo {
    suspend fun getVersionInfo(): GetVersionDataModel.Response?
}