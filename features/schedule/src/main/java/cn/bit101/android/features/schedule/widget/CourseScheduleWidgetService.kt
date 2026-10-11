package cn.bit101.android.features.schedule.widget

import android.content.Intent
import android.widget.RemoteViewsService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Android 8–11 使用此服务加载小组件列表。 */
@AndroidEntryPoint
class CourseScheduleWidgetService : RemoteViewsService() {
    @Inject
    lateinit var widget: CourseScheduleWidget

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = widget.createFactory(intent)
}
