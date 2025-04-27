package cn.bit101.android.data.repo

import cn.bit101.android.config.setting.base.GallerySettings
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.PosterRepo
import cn.bit101.api.model.common.PostersFilter
import cn.bit101.api.model.common.PostersMode
import cn.bit101.api.model.common.PostersOrder
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.PostPostersDataModel
import cn.bit101.api.model.http.bit101.PutPosterDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultPosterRepo @Inject constructor(
    apiManager: APIManager,
    private val gallerySettings: GallerySettings
) : PosterRepo {

    private val api = apiManager.api

    suspend fun getPosters(mode: PostersMode? = null,
                           order: String? = null,
                           page: Long? = null,
                           search: String? = null,
                           uid: Int? = null,
                           noBot: Boolean = false
    ): GetPostersDataModel.Response {
        val datas = api.posters.getPosters(mode,order,page,search,uid).body() ?: throw Exception("get posters error")

        if(noBot)
            datas.removeIf { it.tags.contains("bot") }

        return datas
    }

    override suspend fun getRecommendPosters(
        page: Long?,
    ) = withContext(Dispatchers.IO) {
        getPosters(
            page = page,
            noBot = gallerySettings.hideBotPoster.get(),
        )
    }

    override suspend fun getHotPosters(
        page: Long?,
    ) = withContext(Dispatchers.IO) {
        getPosters(
            mode = PostersMode.hot,
            page = page,
            noBot = gallerySettings.hideBotPoster.get(),
        )
    }

    override suspend fun getFollowPosters(
        page: Long?
    ) = withContext(Dispatchers.IO) {
        getPosters(
            mode = PostersMode.follow,
            page = page,
            noBot = false
        )
    }

    override suspend fun getSearchPosters(
        search: String,
        page: Long?,
        order: String?,
        uid: Int?
    ) = withContext(Dispatchers.IO) {
        getPosters(
            search = search,
            mode = PostersMode.search,
            page = page,
            order = order,
            uid = uid,
            noBot = gallerySettings.hideBotPoster.get() && gallerySettings.hideBotPosterInSearch.get()
        )
    }

    override suspend fun getNewestPosters(
        page: Long?
    ) = withContext(Dispatchers.IO) {
        getPosters(
            mode = PostersMode.search,
            page = page,
            order = PostersOrder.NEW,
            uid = PostersFilter.PUBLIC_ANONYMOUS,
            noBot = gallerySettings.hideBotPoster.get(),
        )
    }

    override suspend fun getPosterById(
        id: Long
    ) = withContext(Dispatchers.IO) {
        api.posters.getPosterById(
            id = id
        ).body() ?: throw Exception("get poster error")
    }

    override suspend fun getPostersOfUserByUid(
        uid: Long,
        page: Long?
    ) = withContext(Dispatchers.IO) {
        if(uid.toInt() == -1) emptyList()
        else getPosters(
            mode = PostersMode.search,
            page = page,
            uid = uid.toInt(),
            noBot = false
        )
    }

    override suspend fun getCommentsById(
        id: Long,
        page: Int?,
        order: String
    ) = withContext(Dispatchers.IO) {
        api.reaction.getComments(
            obj = "poster$id",
            page = page,
            order = order,
        ).body() ?: throw Exception("get comments error")
    }

    override suspend fun getCommentsOfCommentById(
        id: Long,
        page: Int?,
        order: String
    ) = withContext(Dispatchers.IO) {
        api.reaction.getComments(
            obj = "comment$id",
            page = page,
            order = order,
        ).body() ?: throw Exception("get comments error")
    }

    override suspend fun deletePosterById(id: Long) = withContext(Dispatchers.IO) {
        api.posters.deletePoster(id.toInt()).body() ?: throw Exception("delete poster error")
    }

    override suspend fun getClaims() = withContext(Dispatchers.IO) {
        api.posters.getPosterClaims().body() ?: throw Exception("get claim error")
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
        api.posters.postPoster(PostPostersDataModel.Body(
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
        api.posters.putPoster(
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