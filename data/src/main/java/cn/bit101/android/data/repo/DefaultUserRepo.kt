package cn.bit101.android.data.repo

import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.UserRepo
import cn.bit101.api.model.http.bit101.PutUserInfoDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultUserRepo @Inject constructor(
    private val apiManager: APIManager
) : UserRepo {

    private val api = apiManager.api

    override suspend fun getUserInfo(
        id: Long
    ) = withContext(Dispatchers.IO) {
        api.user.getUserInfo(id.toString()).body() ?: throw Exception("获取用户信息失败")
    }

    override suspend fun follow(
        id: Long
    ) = withContext(Dispatchers.IO) {
        api.user.follow(id.toInt()).body() ?: throw Exception("关注失败")
    }

    override suspend fun getFollowers(
        page: Int?
    ) = withContext(Dispatchers.IO) {
        api.user.getFollowers(
            page = page
        ).body() ?: throw Exception("获取粉丝失败")
    }

    override suspend fun getFollowings(
        page: Int?
    ) = withContext(Dispatchers.IO) {
        api.user.getFollowings(
            page = page
        ).body() ?: throw Exception("获取关注失败")
    }

    override suspend fun updateUser(
        avatarMid: String,
        nickname: String,
        motto: String
    ) = withContext(Dispatchers.IO) {
        api.user.putUserInfo(PutUserInfoDataModel.Body(
            avatarMid = avatarMid,
            nickname = nickname,
            motto = motto,
        )).body() ?: throw Exception("更新用户信息失败")
    }
}