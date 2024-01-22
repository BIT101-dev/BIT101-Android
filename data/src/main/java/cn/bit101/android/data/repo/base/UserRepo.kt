package cn.bit101.android.data.repo.base

import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetUserInfoDataModel
import cn.bit101.api.model.http.bit101.PostFollowDataModel

interface UserRepo {

    /**
     * 获取用户信息
     */
    suspend fun getUserInfo(id: Long): GetUserInfoDataModel.Response

    /**
     * 关注/取消关注用户
     */
    suspend fun follow(id: Long): PostFollowDataModel.Response

    /**
     * 获取我的粉丝
     */
    suspend fun getFollowers(page: Int? = null): List<User>

    /**
     * 获取我的关注
     */
    suspend fun getFollowings(page: Int? = null): List<User>

    /**
     * 更新用户信息
     */
    suspend fun updateUser(
        avatarMid: String,
        nickname: String,
        motto: String
    )
}