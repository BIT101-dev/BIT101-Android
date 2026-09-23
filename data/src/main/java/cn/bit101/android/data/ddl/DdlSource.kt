package cn.bit101.android.data.ddl

import cn.bit101.android.data.eclass.EclassDdlLogic

/**
 * DDL 条目的来源。
 *
 * `ddl_schedule` 用 `group` 字段区分来源，但它的取值是**技术标识**
 * （`lexue` / `main` / `eclass`），直接显示给用户会出现「分组：lexue」这种文案
 * —— 桌面组件的 DDL 行首就踩过这个坑（`WidgetLogic.ddlPage` 里 `lead = group`）。
 *
 * ⚠️ 放在 `data` 层而不是某个 feature 模块：**App 与桌面组件都要用它**，
 * 而 `features:widget` 并不依赖 `features:schedule`，放在任一侧另一侧就取不到。
 */
object DdlSource {

    /** 乐学（Moodle）—— 学校已停用，但已同步的历史数据保留。 */
    const val LEXUE = "lexue"

    /** 用户手动添加的日程（`DDLScheduleViewModel.addDDL` 的默认值）。 */
    const val CUSTOM = "main"

    /** 课程中心（eclass）—— 学校 2026 年起的作业来源。 */
    const val ECLASS = EclassDdlLogic.GROUP

    /** 展示用的中文名。 */
    fun displayName(group: String): String = when (group) {
        LEXUE -> "乐学"
        ECLASS -> "课程中心"
        CUSTOM, "" -> "自定义"
        // 未知来源原样显示：将来新增源时不会变成空白，也便于排查
        else -> group
    }

    /**
     * 是否允许编辑 / 删除。
     *
     * ⚠️ 只有**用户手动的**条目可改：来自乐学或课程中心的条目由同步生成，
     * 改了下次同步就会被覆盖回去（同步逻辑只保留 `done` 状态），
     * 让用户去改一个必然被冲掉的字段是误导。
     */
    fun isEditable(group: String): Boolean = group != LEXUE && group != ECLASS
}
