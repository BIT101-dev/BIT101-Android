package cn.bit101.android.data.repo.base

import cn.bit101.api.model.http.app.GetVersionDataModel

interface VersionRepo {

    /**
     * 获取版本信息
     */
    suspend fun getVersionInfo(): GetVersionDataModel.Response
}