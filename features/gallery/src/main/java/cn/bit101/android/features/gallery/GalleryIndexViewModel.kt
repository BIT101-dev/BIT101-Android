package cn.bit101.android.features.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.config.setting.base.GallerySettings
import cn.bit101.android.data.repo.base.PosterRepo
import cn.bit101.android.features.common.helper.RefreshAndLoadMoreStatesCombinedOne
import cn.bit101.android.features.common.helper.RefreshAndLoadMoreStatesCombinedZero
import cn.bit101.api.model.common.PostersFilter
import cn.bit101.api.model.common.PostersOrder
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.zip
import java.io.Serializable
import javax.inject.Inject

internal data class SearchData(
    val search: String,
    val order: String,
    val filter: Int,
) : Serializable {
    companion object {
        val default = SearchData("", PostersOrder.NEW, PostersFilter.PUBLIC_ANONYMOUS)
    }
}


@HiltViewModel
internal class GalleryIndexViewModel @Inject constructor(
    private val posterRepo: PosterRepo,
    private val gallerySettings: GallerySettings,
) : ViewModel() {
    private val newLoadMode = gallerySettings.hideBotPoster.flow

    private val _recommendState = object : RefreshAndLoadMoreStatesCombinedZero<GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh() = refresh(
            newLoadMode,
            refresh = { posterRepo.getRecommendPosters() },
            loadMore = { page -> posterRepo.getRecommendPosters(page) }
        )
        override fun loadMore() = loadMore(newLoadMode) { page -> posterRepo.getRecommendPosters(page) }
    }
    val recommendStateExport = _recommendState.export()

    private val _hotState = object : RefreshAndLoadMoreStatesCombinedZero<GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh() = refresh(
            newLoadMode,
            refresh = { posterRepo.getHotPosters() },
            loadMore = { page -> posterRepo.getHotPosters(page) }
        )
        override fun loadMore() = loadMore(newLoadMode) { page -> posterRepo.getHotPosters(page) }
    }
    val hotStateExport = _hotState.export()

    private val _followState = object : RefreshAndLoadMoreStatesCombinedZero<GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh() = refresh(
            newLoadMode,
            refresh = { posterRepo.getFollowPosters() },
            loadMore = { page -> posterRepo.getFollowPosters(page) }
        )
        override fun loadMore() = loadMore(newLoadMode) { page -> posterRepo.getFollowPosters(page) }
    }
    val followStateExport = _followState.export()

    private val _newestStata = object : RefreshAndLoadMoreStatesCombinedZero<GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh() = refresh(
            newLoadMode,
            refresh = { posterRepo.getNewestPosters() },
            loadMore = { page -> posterRepo.getNewestPosters(page) }
        )
        override fun loadMore() = loadMore(newLoadMode) { page -> posterRepo.getNewestPosters(page) }
    }
    val newestStataExport = _newestStata.export()

    private val _searchState = object : RefreshAndLoadMoreStatesCombinedOne<SearchData, GetPostersDataModel.ResponseItem>(viewModelScope) {
        private var searchData = SearchData.default

        private val newModeFlow = newLoadMode.zip(gallerySettings.hideBotPosterInSearch.flow) { a, b -> a && b }

        override fun refresh(data: SearchData) = refresh(
            newModeFlow,
            refresh = {
                searchData = data
                posterRepo.getSearchPosters(
                    search = searchData.search,
                    order = searchData.order,
                    uid = searchData.filter,
                    page = 0,
                ).toMutableList()
            },
            loadMore = { page ->
                posterRepo.getSearchPosters(
                    search = searchData.search,
                    order = searchData.order,
                    uid = searchData.filter,
                    page = page,
                )
            }
        )

        override fun loadMore(data: SearchData) = loadMore(newModeFlow) { page ->
            posterRepo.getSearchPosters(
                search = searchData.search,
                order = searchData.order,
                uid = searchData.filter,
                page = page,
            )
        }
    }
    val searchStateExports = _searchState.export()

    val enableHorizontalScrollFlow = gallerySettings.allowHorizontalScroll.flow
}