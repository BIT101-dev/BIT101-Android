package cn.bit101.android.data.repo.base

import cn.bit101.api.model.common.ReportType

interface ManageRepo {

    /**
     * 举报帖子
     */
    suspend fun reportPoster(
        id: Long,
        typeId: Long,
        text: String,
    )

    /**
     * 举报评论
     */
    suspend fun reportComment(
        id: Long,
        typeId: Long,
        text: String,
    )

    /**
     * 获取所有举报类型
     */
    suspend fun getReportTypes(): List<ReportType>
}