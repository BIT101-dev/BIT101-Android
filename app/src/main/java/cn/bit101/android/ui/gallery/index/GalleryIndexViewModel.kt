package cn.bit101.android.ui.gallery.index

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.repo.base.PosterRepo
import cn.bit101.android.ui.common.RefreshAndLoadMoreStatesCombined
import cn.bit101.api.model.common.PostersOrder
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.toGetPostersDataModelResponseItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class GalleryIndexViewModel @Inject constructor(
    private val posterRepo: PosterRepo
) : ViewModel() {

    private val _recommendState = RefreshAndLoadMoreStatesCombined<GetPostersDataModel.ResponseItem>(viewModelScope)
    val recommendStateFlows = _recommendState.flows()

    private val _hotState = RefreshAndLoadMoreStatesCombined<GetPostersDataModel.ResponseItem>(viewModelScope)
    val hotStateFlows = _hotState.flows()

    private val _followState = RefreshAndLoadMoreStatesCombined<GetPostersDataModel.ResponseItem>(viewModelScope)
    val followStateFlows = _followState.flows()

    private val _newestStata = RefreshAndLoadMoreStatesCombined<GetPostersDataModel.ResponseItem>(viewModelScope)
    val newestStataFlows = _newestStata.flows()

    private val _searchState = RefreshAndLoadMoreStatesCombined<GetPostersDataModel.ResponseItem>(viewModelScope)
    val searchStateFlows = _searchState.flows()

    val queryLiveData = MutableLiveData("")
    val selectOrderLiveData = MutableLiveData(PostersOrder.NEW)

    val lastSearchQueryLiveData = MutableLiveData("")

    fun setQuery(query: String) {
        queryLiveData.value = query
    }

    fun setSelectOrder(order: String) {
        selectOrderLiveData.value = order
    }

    fun refreshRecommend() = _recommendState.refresh {
        posterRepo.getRecommendPosters()
    }

    fun loadMoreRecommend() = _recommendState.loadMore { page ->
        posterRepo.getRecommendPosters(page)
    }


    fun refreshHot() = _hotState.refresh {
        posterRepo.getHotPosters()
    }

    fun loadMoreHot() = _hotState.loadMore { page ->
        posterRepo.getHotPosters(page)
    }


    fun refreshFollow() = _followState.refresh {
        posterRepo.getFollowPosters()
    }

    fun loadMoreFollow() = _followState.loadMore { page ->
        posterRepo.getFollowPosters(page)
    }

    fun refreshSearch(
        search: String,
        order: String,
        filter: Int,
    ) = _searchState.refresh {
        val posters = posterRepo.getSearchPosters(
            search = search,
            order = order,
            uid = filter,
            page = 0,
        ).toMutableList()

        /**
         * 根据id搜索
         */
        try {
            val id = search.toLong()
            val poster = posterRepo.getPosterById(id).toGetPostersDataModelResponseItem()
            posters.add(0, poster)
        } catch (_: Exception) { }

        lastSearchQueryLiveData.postValue(search)

        posters
    }

    fun loadMoreSearch(
        search: String,
        order: String,
        filter: Int,
    ) = _searchState.loadMore { page ->
        posterRepo.getSearchPosters(
            search = search,
            page = page,
            order = order,
            uid = filter,
        )
    }

    fun refreshNewest() = _newestStata.refresh {
        posterRepo.getNewestPosters()
    }

    fun loadMoreNewest() = _newestStata.loadMore { page ->
        posterRepo.getNewestPosters(page)
    }

}