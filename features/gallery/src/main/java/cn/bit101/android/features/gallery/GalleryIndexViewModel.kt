package cn.bit101.android.features.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.config.setting.base.GallerySettings
import cn.bit101.android.data.repo.base.PosterRepo
import cn.bit101.android.features.common.helper.FilteredStateOne
import cn.bit101.android.features.common.helper.FilteredStateZero
import cn.bit101.android.features.common.helper.RefreshAndLoadMoreStatesCombinedZero
import cn.bit101.api.model.common.PostersFilter
import cn.bit101.api.model.common.PostersOrder
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import java.io.Serializable
import javax.inject.Inject
import kotlin.collections.toMutableList

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

    private fun filterer(poster: GetPostersDataModel.ResponseItem, hideUids: Set<Int>): Boolean =
        !hideUids.contains(poster.user.id)
    private val filterDataFlow = gallerySettings.hideUserUids.flow.map { it.toSet() }

    private val _recommendState = FilteredStateZero(viewModelScope, posterRepo::getRecommendPosters, ::filterer, filterDataFlow)
    val recommendStateExport = _recommendState.export()

    private val _hotState = FilteredStateZero(viewModelScope, posterRepo::getHotPosters, ::filterer, filterDataFlow)
    val hotStateExport = _hotState.export()

    private val _followState = object : RefreshAndLoadMoreStatesCombinedZero<GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh() = refresh { posterRepo.getFollowPosters() }
        override fun loadMore() = loadMore { page -> posterRepo.getFollowPosters(page) }
    }
    val followStateExport = _followState.export()

    private val _newestStata = FilteredStateZero(viewModelScope, posterRepo::getNewestPosters, ::filterer, filterDataFlow)
    val newestStataExport = _newestStata.export()

    private var _searchData = SearchData.default
    private val _searchState = FilteredStateOne<SearchData, GetPostersDataModel.ResponseItem, Set<Int>>(
        viewModelScope,
        { data, page ->
            if (page == null) {
                _searchData = data
                posterRepo.getSearchPosters(
                    search = _searchData.search,
                    order = _searchData.order,
                    uid = _searchData.filter,
                    page = 0,
                ).toMutableList()
            } else {
                posterRepo.getSearchPosters(
                    search = _searchData.search,
                    order = _searchData.order,
                    uid = _searchData.filter,
                    page = page,
                )
            }
        },
        ::filterer,
        filterDataFlow,
    )
    val searchStateExports = _searchState.export()

    val enableHorizontalScrollFlow = gallerySettings.allowHorizontalScroll.flow
}