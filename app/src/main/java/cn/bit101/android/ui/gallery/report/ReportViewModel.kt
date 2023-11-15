package cn.bit101.android.ui.gallery.report

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.bit101.android.repo.base.ManageRepo
import cn.bit101.android.ui.gallery.common.SimpleDataState
import cn.bit101.android.ui.gallery.common.SimpleState
import cn.bit101.api.model.common.ReportType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val manageRepo: ManageRepo
) : ViewModel() {

    val stateLiveData = MutableLiveData<SimpleState>(null)

    private val _loadReportTypeStateFlow = MutableStateFlow<SimpleDataState<List<ReportType>>?>(null)
    val loadReportTypeStateFlow = _loadReportTypeStateFlow.asStateFlow()

    val selectedReportTypeLiveData = MutableLiveData<ReportType>(null)

    private val _textFlow = MutableStateFlow("")
    val textFlow = _textFlow.asStateFlow()

    fun setSelectedReportType(reportType: ReportType) {
        selectedReportTypeLiveData.value = reportType
    }

    fun setText(text: String) {
        _textFlow.value = text
    }

    fun loadReportType() {
        _loadReportTypeStateFlow.value = SimpleDataState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val reportTypes = manageRepo.getReportTypes()
                _loadReportTypeStateFlow.value = SimpleDataState.Success(reportTypes)
            } catch (e: Exception) {
                e.printStackTrace()
                _loadReportTypeStateFlow.value = SimpleDataState.Error()
            }
        }
    }


    fun reportPoster(id: Long, typeId: Long, text: String) {
        stateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                manageRepo.reportPoster(id, typeId, text)
                stateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                stateLiveData.postValue(SimpleState.Error)
            }
        }
    }

    fun reportComment(id: Long, typeId: Long, text: String) {
        stateLiveData.value = SimpleState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                manageRepo.reportComment(id, typeId, text)
                stateLiveData.postValue(SimpleState.Success)
            } catch (e: Exception) {
                e.printStackTrace()
                stateLiveData.postValue(SimpleState.Error)
            }
        }
    }
}