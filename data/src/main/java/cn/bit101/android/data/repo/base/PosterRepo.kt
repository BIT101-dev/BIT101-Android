package cn.bit101.android.data.repo.base

import cn.bit101.api.model.common.Claim
import cn.bit101.api.model.http.bit101.GetCommentsDataModel
import cn.bit101.api.model.http.bit101.GetPosterDataModel
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.PostPostersDataModel

interface PosterRepo {
    /**
     * 获取推荐帖子列表
     */
    suspend fun getRecommendPosters(
        page: Long? = null,
    ): List<GetPostersDataModel.ResponseItem>

    /**
     * 获取热门帖子列表
     */
    suspend fun getHotPosters(
        page: Long? = null,
    ): List<GetPostersDataModel.ResponseItem>

    /**
     * 获取关注帖子列表
     */
    suspend fun getFollowPosters(
        page: Long? = null,
    ): List<GetPostersDataModel.ResponseItem>

    /**
     * 搜索帖子
     */
    suspend fun getSearchPosters(
        search: String,
        page: Long? = null,
        order: String? = null,
        uid: Int? = null,
    ): List<GetPostersDataModel.ResponseItem>

    /**
     * 获取帖子，按照时间排序，最新的在前面
     */
    suspend fun getNewestPosters(
        page: Long? = null,
    ): List<GetPostersDataModel.ResponseItem>

    /**
     * 通过 id 获取帖子
     */
    suspend fun getPosterById(
        id: Long,
    ): GetPosterDataModel.Response

    /**
     * 通过用户的 uid 获取他发的帖子列表
     */
    suspend fun getPostersOfUserByUid(
        uid: Long,
        page: Long? = null,
    ): List<GetPostersDataModel.ResponseItem>

    /**
     * 获取Poster的评论，应该写在reaction那里的
     */
    suspend fun getCommentsById(
        id: Long,
        page: Int? = null,
    ): GetCommentsDataModel.Response

    /**
     * 获取Comment的评论，应该写在reaction那里的
     */
    suspend fun getCommentsOfCommentById(
        id: Long,
        page: Int? = null,
    ): GetCommentsDataModel.Response

    /**
     * 删除 id 对应的帖子
     */
    suspend fun deletePosterById(
        id: Long,
    )

    /**
     * 获取所有的创作者声明
     */
    suspend fun getClaims(): List<Claim>

    /**
     * 发布帖子
     */
    suspend fun post(
        anonymous: Boolean,
        claimId: Int,
        imageMids: List<String>,
        plugins: String = "[]",
        public: Boolean,
        tags: List<String>,
        text: String,
        title: String,
    ): PostPostersDataModel.Response

    /**
     * 更新帖子
     */
    suspend fun update(
        id: Long,
        anonymous: Boolean,
        claimId: Int,
        imageMids: List<String>,
        plugins: String = "[]",
        public: Boolean,
        tags: List<String>,
        text: String,
        title: String,
    )
}