package cn.bit101.api.model.http.bit101

import cn.bit101.api.model.common.Avatar
import cn.bit101.api.model.common.Identity
import cn.bit101.api.model.common.User

// 学校统一身份验证初始化
class PostWebvpnVerifyInitDataModel private constructor() {
    data class Body(
        // 学号
        val sid: String,
    )

    data class Response(
        // 验证码图片链接
        val captcha: String,
        val cookie: String,
        val execution: String,
        val salt: String
    )
}

// 学校统一身份认证验证
class PostWebvpnVerifyDataModel private constructor() {
    data class Body(
        val sid: String,

        // 加密后的密码
        val password: String,
        val execution: String,
        val cookie: String,

        // 验证码，为空则不需要验证码
        val captcha: String? = null,
    )

    data class Response(
        val token: String,
        val code: String
    )
}

// 发送邮箱验证码
class PostMailVerifyDataModel private constructor() {
    data class Body(
        val sid: String,
    )

    data class Response(
        val token: String,
    )
}


// 注册/重置密码/code登录
class PostRegisterDataModel private constructor() {
    data class Body(
        val password: String,
        val token: String,
        val code: String,

        // 当true时，如果用户已注册，将不会修改密码，但如果用户没有注册过，将进行注册操作。
        val loginMode: Boolean? = null,
    ) {
    }

    data class Response(
        val fakeCookie: String
    )
}

// 登录
class PostLoginDataModel private constructor() {
    data class Body(
        val sid: String,
        val password: String,
    )

    data class Response(
        val fakeCookie: String
    )
}

// 用户信息，需要登录
class GetUserInfoDataModel private constructor() {
    data class Response(
        val user: User,

        // 关注数量
        val followingNum: Int,

        // 粉丝数量
        val followerNum: Int,

        // 是否被我关注
        val following: Boolean,

        // 是否关注我
        val follower: Boolean,

        // 是否为当前登录用户
        val own: Boolean,
    )
}

// 修改用户信息，需要登录
class PutUserInfoDataModel private constructor() {
    data class Body(
        // 昵称，为空则不改变
        val nickname: String? = null,

        // 简介/格言，为空则不改变
        val motto: String? = null,

        // 头像图片mid，为空则不改变
        val avatarMid: String? = null,
    )
}

// 关注，需要登录
class PostFollowDataModel private constructor() {
    data class Response(
        // 是否被我关注
        val following: Boolean,

        // 是否关注我
        val follower: Boolean,

        // 关注数量
        val followingNum: Int,

        // 粉丝数量
        val followerNum: Int,
    )
}

// 获取关注列表，需要登录
class GetFollowingsDataModel private constructor() {
    data class ResponseItem(
        val id: Int,

        // 注册时间
        val createTime: String,

        // 昵称
        val nickname: String,

        // 头像
        val avatar: Avatar,

        // 格言/简介
        val motto: String,

        // 身份
        val identity: Identity,
    )

    class Response : ArrayList<ResponseItem>()
}

// 获取粉丝列表，需要登录
class GetFollowersDataModel private constructor() {
    data class ResponseItem(
        val id: Int,

        // 注册时间
        val createTime: String,

        // 昵称
        val nickname: String,

        // 头像
        val avatar: Avatar,

        // 格言/简介
        val motto: String,

        // 身份
        val identity: Identity,
    )

    class Response : ArrayList<ResponseItem>()
}