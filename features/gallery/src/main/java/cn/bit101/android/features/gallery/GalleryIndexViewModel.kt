package cn.bit101.android.features.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.data.repo.base.PosterRepo
import cn.bit101.android.features.common.helper.RefreshAndLoadMoreStatesCombinedOne
import cn.bit101.android.features.common.helper.RefreshAndLoadMoreStatesCombinedZero
import cn.bit101.api.model.common.PostersFilter
import cn.bit101.api.model.common.PostersOrder
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.Serializable
import javax.inject.Inject

data class SearchData(
    val search: String,
    val order: String,
    val filter: Int,
) : Serializable {
    companion object {
        val default = SearchData("", PostersOrder.NEW, PostersFilter.PUBLIC_ANONYMOUS)
    }
}


@HiltViewModel
class GalleryIndexViewModel @Inject constructor(
    private val posterRepo: PosterRepo
) : ViewModel() {

    private val _recommendState = object : RefreshAndLoadMoreStatesCombinedZero<GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh() = refresh { posterRepo.getRecommendPosters() }
        override fun loadMore() = loadMore { page -> posterRepo.getRecommendPosters(page) }
    }
    val recommendStateExport = _recommendState.export()

    private val _hotState = object : RefreshAndLoadMoreStatesCombinedZero<GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh() = refresh { posterRepo.getHotPosters() }
        override fun loadMore() = loadMore { page -> posterRepo.getHotPosters(page) }
    }
    val hotStateExport = _hotState.export()

    private val _followState = object : RefreshAndLoadMoreStatesCombinedZero<GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh() = refresh { posterRepo.getFollowPosters() }
        override fun loadMore() = loadMore { page -> posterRepo.getFollowPosters(page) }
    }
    val followStateExport = _followState.export()

    private val _newestStata = object : RefreshAndLoadMoreStatesCombinedZero<GetPostersDataModel.ResponseItem>(viewModelScope) {
        override fun refresh() = refresh { posterRepo.getNewestPosters() }
        override fun loadMore() = loadMore { page -> posterRepo.getNewestPosters(page) }
    }
    val newestStataExport = _newestStata.export()

    private val _searchState = object : RefreshAndLoadMoreStatesCombinedOne<SearchData, GetPostersDataModel.ResponseItem>(viewModelScope) {
        private var searchData = SearchData.default

        override fun refresh(data: SearchData) = refresh {
            searchData = data
            posterRepo.getSearchPosters(
                search = searchData.search,
                order = searchData.order,
                uid = searchData.filter,
                page = 0,
            ).toMutableList()
        }

        override fun loadMore(data: SearchData) = loadMore { page ->
            posterRepo.getSearchPosters(
                search = searchData.search,
                order = searchData.order,
                uid = searchData.filter,
                page = page,
            )
        }
    }
    val searchStateExports = _searchState.export()

}