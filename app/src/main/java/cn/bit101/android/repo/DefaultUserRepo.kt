package cn.bit101.android.repo

import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.UserRepo
import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.PostFollowDataModel
import cn.bit101.api.model.http.bit101.PutUserInfoDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultUserRepo @Inject constructor() : UserRepo {
    override suspend fun getUserInfo(
        id: Long
    ) = withContext(Dispatchers.IO) {
        BIT101API.user.getUserInfo(id.toString()).body() ?: throw Exception("获取用户信息失败")
    }

    override suspend fun follow(
        id: Long
    ) = withContext(Dispatchers.IO) {
        BIT101API.user.follow(id.toInt()).body() ?: throw Exception("关注失败")
    }

    override suspend fun getFollowers(
        page: Int?
    ) = withContext(Dispatchers.IO) {
        BIT101API.user.getFollowers(
            page = page
        ).body() ?: throw Exception("获取粉丝失败")
    }

    override suspend fun getFollowings(
        page: Int?
    ) = withContext(Dispatchers.IO) {
        BIT101API.user.getFollowings(
            page = page
        ).body() ?: throw Exception("获取关注失败")
    }

    override suspend fun updateUser(
        avatarMid: String,
        nickname: String,
        motto: String
    ) = withContext(Dispatchers.IO) {
        BIT101API.user.putUserInfo(PutUserInfoDataModel.Body(
            avatarMid = avatarMid,
            nickname = nickname,
            motto = motto,
        )).body() ?: throw Exception("更新用户信息失败")
    }
}