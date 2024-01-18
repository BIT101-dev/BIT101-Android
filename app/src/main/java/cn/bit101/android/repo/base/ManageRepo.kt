package cn.bit101.android.repo.base

import cn.bit101.api.model.common.ReportType

interface ManageRepo {

    suspend fun reportPoster(
        id: Long,
        typeId: Long,
        text: String,
    )

    suspend fun reportComment(
        id: Long,
        typeId: Long,
        text: String,
    )

    suspend fun getReportTypes(): List<ReportType>
}