package cn.bit101.android.repo.base

import cn.bit101.api.model.common.Claim
import cn.bit101.api.model.http.bit101.GetCommentsDataModel
import cn.bit101.api.model.http.bit101.GetPosterDataModel
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.PostPostersDataModel

interface PosterRepo {

    suspend fun getRecommendPosters(
        page: Long? = null,
    ): List<GetPostersDataModel.ResponseItem>

    suspend fun getHotPosters(
        page: Long? = null,
    ): List<GetPostersDataModel.ResponseItem>

    suspend fun getFollowPosters(
        page: Long? = null,
    ): List<GetPostersDataModel.ResponseItem>

    suspend fun getSearchPosters(
        search: String,
        page: Long? = null,
        order: String? = null,
        uid: Int? = null,
    ): List<GetPostersDataModel.ResponseItem>

    suspend fun getNewestPosters(
        page: Long? = null,
    ): List<GetPostersDataModel.ResponseItem>

    suspend fun getPosterById(
        id: Long,
    ): GetPosterDataModel.Response

    suspend fun getPostersOfUserByUid(
        id: Long,
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

    suspend fun deletePosterById(
        id: Long,
    )

    suspend fun getClaims(): List<Claim>

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