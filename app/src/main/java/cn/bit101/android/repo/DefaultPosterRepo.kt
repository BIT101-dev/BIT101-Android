package cn.bit101.android.repo

import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.PosterRepo
import cn.bit101.api.model.common.PostersFilter
import cn.bit101.api.model.common.PostersMode
import cn.bit101.api.model.common.PostersOrder
import cn.bit101.api.model.http.bit101.PostPostersDataModel
import cn.bit101.api.model.http.bit101.PutPosterDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultPosterRepo @Inject constructor() : PosterRepo {
    override suspend fun getRecommendPosters(
        page: Long?,
    ) = withContext(Dispatchers.IO) {
        BIT101API.posters.getPosters(
            page = page,
        ).body() ?: throw Exception("get posters error")
    }

    override suspend fun getHotPosters(
        page: Long?,
    ) = withContext(Dispatchers.IO) {
        BIT101API.posters.getPosters(
            mode = PostersMode.hot,
            page = page,
        ).body() ?: throw Exception("get posters error")
    }

    override suspend fun getFollowPosters(
        page: Long?
    ) = withContext(Dispatchers.IO) {
        BIT101API.posters.getPosters(
            mode = PostersMode.follow,
            page = page,
        ).body() ?: throw Exception("get posters error")
    }

    override suspend fun getSearchPosters(
        search: String,
        page: Long?,
        order: PostersOrder?,
        uid: Int?
    ) = withContext(Dispatchers.IO) {
        BIT101API.posters.getPosters(
            search = search,
            mode = PostersMode.search,
            page = page,
            order = order,
            uid = uid,
        ).body() ?: throw Exception("get posters error")
    }

    override suspend fun getNewestPosters(
        page: Long?
    ) = withContext(Dispatchers.IO) {
        BIT101API.posters.getPosters(
            mode = PostersMode.search,
            page = page,
            order = PostersOrder.new,
            uid = PostersFilter.PUBLIC_ANONYMOUS,
        ).body() ?: throw Exception("get posters error")
    }

    override suspend fun getPosterById(
        id: Long
    ) = withContext(Dispatchers.IO) {
        BIT101API.posters.getPosterById(
            id = id
        ).body() ?: throw Exception("get poster error")
    }

    override suspend fun getCommentsById(
        id: Long,
        page: Int?,
    ) = withContext(Dispatchers.IO) {
        BIT101API.reaction.getComments(
            obj = "poster$id",
            page = page,
        ).body() ?: throw Exception("get comments error")
    }

    override suspend fun getCommentsOfCommentById(
        id: Long,
        page: Int?
    ) = withContext(Dispatchers.IO) {
        BIT101API.reaction.getComments(
            obj = "comment$id",
            page = page,
        ).body() ?: throw Exception("get comments error")
    }

    override suspend fun deletePosterById(id: Long) = withContext(Dispatchers.IO) {
        BIT101API.posters.deletePoster(id.toInt()).body() ?: throw Exception("delete poster error")
        Unit
    }

    override suspend fun getClaim() = withContext(Dispatchers.IO) {
        BIT101API.posters.getPosterClaims().body() ?: throw Exception("get claim error")
    }

    override suspend fun post(
        anonymous: Boolean,
        claimId: Int,
        imageMids: List<String>,
        plugins: String,
        public: Boolean,
        tags: List<String>,
        text: String,
        title: String
    ) = withContext(Dispatchers.IO) {
        BIT101API.posters.postPoster(PostPostersDataModel.Body(
            anonymous = anonymous,
            claimId = claimId,
            imageMids = imageMids,
            plugins = plugins,
            public = public,
            tags = tags,
            text = text,
            title = title,
        )).body() ?: throw Exception("post poster error")
    }

    override suspend fun update(
        id: Long,
        anonymous: Boolean,
        claimId: Int,
        imageMids: List<String>,
        plugins: String,
        public: Boolean,
        tags: List<String>,
        text: String,
        title: String
    ) = withContext(Dispatchers.IO) {
        BIT101API.posters.putPoster(
            id = id.toInt(),
            body = PutPosterDataModel.Body(
                anonymous = anonymous,
                claimId = claimId,
                imageMids = imageMids,
                plugins = plugins,
                public = public,
                tags = tags,
                text = text,
                title = title,
            )
        ).body() ?: throw Exception("update poster error")
    }
}