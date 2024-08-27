# `data` 模块

- 依赖：`api`、`config`
- 作用：从 database 与 net 中获取数据，更改 database 中的数据。
- 结构：
  - `cn.bit101.android.data.common`: 通用工具类
  - `cn.bit101.android.data.database`: 数据库
  - `cn.bit101.android.data.net`: 网络
  - `cn.bit101.android.data.repo`: 数据仓库
- 导出：若干个 `Repository` 用于数据交互操作