package cn.bit101.android.repo

import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultUserRepo @Inject constructor() : UserRepo {
    override suspend fun getUserInfo(
        id: Long
    ) = withContext(Dispatchers.IO) {
        BIT101API.user.getUserInfo(id.toString()).body() ?: throw Exception("获取用户信息失败")
    }

}