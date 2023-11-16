package cn.bit101.android.repo.base

import cn.bit101.api.model.http.bit101.GetUserInfoDataModel

interface UserRepo {
    suspend fun getUserInfo(id: Long): GetUserInfoDataModel.Response
}