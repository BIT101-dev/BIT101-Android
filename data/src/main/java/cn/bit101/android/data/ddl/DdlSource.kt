package cn.bit101.android.data.ddl

import cn.bit101.android.data.eclass.EclassDdlLogic

/** DDL 来源标识及展示名称。 */
object DdlSource {

    /** 乐学来源。 */
    const val LEXUE = "lexue"

    /** 手动添加的日程。 */
    const val CUSTOM = "main"

    /** 课程中心来源。 */
    const val ECLASS = EclassDdlLogic.GROUP

    /** 展示用的中文名。 */
    fun displayName(group: String): String = when (group) {
        LEXUE -> "乐学"
        ECLASS -> "课程中心"
        CUSTOM, "" -> "自定义"
        // 保留未知来源，避免显示为空。
        else -> group
    }

    /** 只有手动添加的条目允许编辑。 */
    fun isEditable(group: String): Boolean = group != LEXUE && group != ECLASS
}
