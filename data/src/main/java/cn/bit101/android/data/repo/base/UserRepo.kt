package cn.bit101.android.data.repo.base

import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import cn.bit101.api.model.http.bit101.PostFollowDataModel

interface UserRepo {

    suspend fun getUserInfo(id: Long): GetUserInfoDataModel.Response

    suspend fun follow(id: Long): PostFollowDataModel.Response

    suspend fun getFollowers(page: Int? = null): List<User>

    suspend fun getFollowings(page: Int? = null): List<User>

    suspend fun updateUser(
        avatarMid: String,
        nickname: String,
        motto: String
    )
}