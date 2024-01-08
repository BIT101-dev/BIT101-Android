package cn.bit101.android.ui.gallery.index

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.repo.base.PosterRepo
import cn.bit101.android.ui.common.RefreshAndLoadMoreStatesCombinedOne
import cn.bit101.android.ui.common.RefreshAndLoadMoreStatesCombinedZero
import cn.bit101.api.model.common.PostersOrder
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.toGetPostersDataModelResponseItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchData(
    val search: String,
    val order: String,
    val filter: Int,
)


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
        override fun refresh(data: SearchData) = refresh {
            val posters = posterRepo.getSearchPosters(
                search = data.search,
                order = data.order,
                uid = data.filter,
                page = 0,
            ).toMutableList()

            /**
             * 根据id搜索
             */
            try {
                val id = data.search.toLong()
                val poster = posterRepo.getPosterById(id).toGetPostersDataModelResponseItem()
                posters.add(0, poster)
            } catch (_: Exception) { }

            lastSearchQueryLiveData.postValue(data.search)

            posters
        }

        override fun loadMore(data: SearchData) = loadMore { page ->
            posterRepo.getSearchPosters(
                search = data.search,
                page = page,
                order = data.order,
                uid = data.filter,
            )
        }
    }
    val searchStateFlows = _searchState.export()

    private val _searchDataFlow = MutableStateFlow(SearchData("", PostersOrder.NEW, 0))
    val searchDataFlow = _searchDataFlow.asStateFlow()

    val lastSearchQueryLiveData = MutableLiveData("")

    fun setSearchData(searchData: SearchData) {
        _searchDataFlow.value = searchData
    }

}