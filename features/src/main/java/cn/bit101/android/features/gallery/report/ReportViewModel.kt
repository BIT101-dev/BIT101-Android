package cn.bit101.android.features.gallery.report

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.bit101.android.data.repo.base.ManageRepo
import cn.bit101.android.features.common.SimpleDataState
import cn.bit101.android.features.common.SimpleState
import cn.bit101.android.features.common.withSimpleDataStateFlow
import cn.bit101.android.features.common.withSimpleStateLiveData
import cn.bit101.api.model.common.ReportType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val manageRepo: ManageRepo
) : ViewModel() {

    val stateLiveData = MutableLiveData<SimpleState?>(null)

    private val _loadReportTypeStateFlow = MutableStateFlow<SimpleDataState<List<ReportType>>?>(null)
    val loadReportTypeStateFlow = _loadReportTypeStateFlow.asStateFlow()

    val selectedReportTypeLiveData = MutableLiveData<ReportType?>(null)

    private val _textFlow = MutableStateFlow("")
    val textFlow = _textFlow.asStateFlow()

    fun setSelectedReportType(reportType: ReportType) {
        selectedReportTypeLiveData.value = reportType
    }

    fun setText(text: String) {
        _textFlow.value = text
    }

    fun loadReportType() = withSimpleDataStateFlow(_loadReportTypeStateFlow) {
        manageRepo.getReportTypes()
    }


    fun reportPoster(id: Long, typeId: Long, text: String) = withSimpleStateLiveData(stateLiveData) {
        manageRepo.reportPoster(id, typeId, text)
    }

    fun reportComment(id: Long, typeId: Long, text: String) = withSimpleStateLiveData(stateLiveData) {
        manageRepo.reportComment(id, typeId, text)
    }
}