package cn.bit101.android.data.repo

import android.util.Log
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.ManageRepo
import cn.bit101.api.model.http.bit101.PostReportDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultManageRepo @Inject constructor(
    private val apiManager: APIManager,
) : ManageRepo {

    private val api = apiManager.api

    private suspend fun report(
        obj: String,
        typeId: Long,
        text: String,
    ) = withContext(Dispatchers.IO) {

        val res = api.manage.report(PostReportDataModel.Body(
            obj = obj,
            typeId = typeId,
            text = text,
        ))

        Log.i("DefaultManageRepo", "report: ${res.errorBody()?.string()}")

        res.body() ?: throw Exception("report failed")
    }

    override suspend fun reportPoster(
        id: Long,
        typeId: Long,
        text: String
    ) = report("poster$id", typeId, text)


    override suspend fun reportComment(
        id: Long,
        typeId: Long,
        text: String
    ) = report("comment$id", typeId, text)

    override suspend fun getReportTypes() = withContext(Dispatchers.IO) {
        api.manage.getReportTypes().body() ?: throw Exception("get report types failed")
    }

}