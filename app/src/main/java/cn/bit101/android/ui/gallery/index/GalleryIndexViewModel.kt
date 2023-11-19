package cn.bit101.android.ui.gallery.index

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.repo.base.PosterRepo
import cn.bit101.android.ui.gallery.common.StateCombined
import cn.bit101.api.model.common.PostersOrder
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import cn.bit101.api.model.http.bit101.toGetPostersDataModelResponseItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class GalleryIndexViewModel @Inject constructor(
    private val posterRepo: PosterRepo
) : ViewModel() {

    var initSelectedTabIndex = 2

    val recommendStateCombined = StateCombined<GetPostersDataModel.ResponseItem>(viewModelScope)
    val hotStateCombined = StateCombined<GetPostersDataModel.ResponseItem>(viewModelScope)
    val followStateCombined = StateCombined<GetPostersDataModel.ResponseItem>(viewModelScope)
    val newestStataCombined = StateCombined<GetPostersDataModel.ResponseItem>(viewModelScope)
    val searchStateCombined = StateCombined<GetPostersDataModel.ResponseItem>(viewModelScope)

    val queryLiveData = MutableLiveData("")
    val selectOrderLiveData = MutableLiveData(PostersOrder.NEW)

    val lastSearchQueryLiveData = MutableLiveData("")

    fun setQuery(query: String) {
        queryLiveData.value = query
    }

    fun setSelectOrder(order: String) {
        selectOrderLiveData.value = order
    }

    fun refreshRecommend() = recommendStateCombined.refresh {
        posterRepo.getRecommendPosters()
    }

    fun loadMoreRecommend() = recommendStateCombined.loadMore { page ->
        posterRepo.getRecommendPosters(page)
    }


    fun refreshHot() = hotStateCombined.refresh {
        posterRepo.getHotPosters()
    }

    fun loadMoreHot() = hotStateCombined.loadMore { page ->
        posterRepo.getHotPosters(page)
    }


    fun refreshFollow() = followStateCombined.refresh {
        posterRepo.getFollowPosters()
    }

    fun loadMoreFollow() = followStateCombined.loadMore { page ->
        posterRepo.getFollowPosters(page)
    }

    fun refreshSearch(
        search: String,
        order: String,
        filter: Int,
    ) = searchStateCombined.refresh {
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
    ) = searchStateCombined.loadMore { page ->
        posterRepo.getSearchPosters(
            search = search,
            page = page,
            order = order,
            uid = filter,
        )
    }

    fun refreshNewest() = newestStataCombined.refresh {
        posterRepo.getNewestPosters()
    }

    fun loadMoreNewest() = newestStataCombined.loadMore { page ->
        posterRepo.getNewestPosters(page)
    }

}