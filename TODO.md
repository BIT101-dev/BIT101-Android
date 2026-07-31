# BIT101-Android TODO

## 已完成的历史任务（归档）

### BIT-Login 登录迁移（已完成）

- [x] Kotlin/AGP/KSP/Gradle/Compose 编译器工具链升级至与 BIT-Login v4.0.0 兼容的版本（Kotlin 2.4.10、KSP 2.3.10、AGP 8.13.2、Gradle 8.13、compileSdk 36）。
- [x] 在 `api` 模块新增无状态 `SchoolLoginService` 适配层，封装 BIT-Login 的 `JxzxehallLogin`，以 `cookieDetails()` 导出完整 Cookie 属性并正确关闭 Ktor 会话。
- [x] 实现 SDK Cookie → `java.net.HttpCookie` 的桥接（`SchoolCookieStore`），保留 domain/path/secure/过期时间，同步前清除旧学校 Cookie。
- [x] 重写 `DefaultLoginRepo` 学校登录为 BIT-Login，保留 BIT101 后端 `webVpnVerifyInit/webVpnVerify/register` 与 `fake-cookie` 流程。
- [x] `checkLogin()` 校验 BIT101 后端 + 学校会话双端有效才算登录；`doOperationRequiresLogin` 失败后最多重试一次；登录/重认证/注销共用互斥锁。
- [x] 删除旧 `SchoolUserApiService`、登录 DTO、converter 及 `schoolLoginUrl` 配置，清理全部残留引用。
- [x] 单元测试、模块编译、`assembleDebug`、`compileReleaseKotlin` 全部通过。

### 学校 Cookie 过期导致 App 自动退出登录的修复（已完成）

> 背景：学校统一身份认证 Cookie 约一天过期，导致 App 一天内自动退出登录（登录态被清除）。
> 方案：把"校验失败 → 直接登出"改为"校验失败 → 用已存学号密码静默刷新整个会话（学校 Cookie + bit101 fakeCookie）→ 学校要求短信验证时弹出验证码弹窗完成二次验证 → 刷新成功则重试原操作；仅确定性认证失败才登出"。
> 说明：代码与单元测试已全部完成（提交 `ba3ddb2`～`9100fca`）；真机验证项已并入后续任务的"验收标准"统一回归。

- [x] 数据层新增共享短信验证码总线 `SmsCodeRequestHub`，把验证码输入请求从任意页面/后台刷新统一上抛给全局 `SmsCodeDialogHost` 弹窗。
- [x] `LoginRepo` 新增静默刷新能力 `refreshLogin()` 与结果分类 `LoginRefreshResult { SUCCESS, NEEDS_INTERACTIVE, TRANSIENT, FAILED }`。
- [x] `checkLogin()` 改为"验证 → 刷新 → 分类处理"：`checkBit101Login()` + `checkSchoolLogin()` 纯校验；任一侧失效则 `refreshLogin(allowInteractive = true)`；仅确定性认证失败才 `clear()`，临时性失败与短信需求不清除凭据。
- [x] `DefaultAPIManager` bit101Client 401 拦截器改为"静默刷新 + 重试一次"（注入 `Provider<LoginRepo>`，带 `X-BIT101-AUTH-RETRIED` 防死循环）。
- [x] UI 层新增全局 `SmsCodeDialogHost`（挂载于 `MainApp`），`LoginOrLogoutViewModel/Screen` 改用共享总线，移除本地弹窗实现。
- [x] 单元测试：`DefaultLoginRepoTest`（刷新/自愈/失败分类）、`SmsCodeRequestHubTest`（发布/await/cancel/submit）。
- [x] 编译与测试：`sh gradlew :data:compileDebugKotlin`、`sh gradlew testDebugUnitTest`、`sh gradlew assembleDebug` 全部通过。

### 进入登录界面持续转圈 —— `loginMutex` 重入死锁修复（已完成）

> 背景：用户反馈进入登录界面（设置 → 登录，或需要登录的功能入口）后，页面在"检查登录状态"阶段持续转圈，永不进入登录表单；logcat 无异常、无日志。
> 根因：`loginMutex`（`kotlinx.coroutines.sync.Mutex`，非重入）被 `checkLogin()/login()/refreshLogin()` 持锁；bit101 OkHttp 401 拦截器在持锁流程内部调用 `runBlocking { refreshLogin() }` 重入同一把锁 → 拦截器线程等待互斥锁，持锁协程又等待该 HTTP 响应返回 → 循环等待死锁，`checkLogin()` 永不返回。
> 方案：401 拦截器改用非阻塞 `tryRefreshLogin()`（`Mutex.tryLock()`）；锁被占用时返回新增的 `LoginRefreshResult.BUSY`，拦截器直接返回 401、不清除登录态，交由外层持锁流程处理。
> 说明：代码与单元测试已全部完成；真机验证项并入下方"当前任务"统一回归。

- [x] `LoginRepo.kt`：`LoginRefreshResult` 新增 `BUSY`；接口新增 `suspend fun tryRefreshLogin(): LoginRefreshResult`。
- [x] `DefaultLoginRepo.kt`：实现 `tryRefreshLogin()` —— `loginMutex.tryLock()` 成功则持锁执行 `refreshLoginLocked(allowInteractive = false)` 并 `finally` 释放；失败返回 `BUSY`，不触碰任何登录状态。
- [x] `checkLogin()` 的 `when (refreshLoginLocked(...))` 补 `BUSY` 分支（持锁状态下不可能出现，保守按不清理处理），恢复穷尽性。
- [x] `DefaultAPIManager.kt`：401 拦截器改调 `tryRefreshLogin()` —— `SUCCESS` 携带新 fakeCookie + `X-BIT101-AUTH-RETRIED` 重试一次；`BUSY` 直接返回 401 不清除登录态；其余维持原 `clear()` 逻辑。
- [x] 单元测试 `DefaultLoginRepoTest` 新增：持锁时 `tryRefreshLogin` 返回 `BUSY` 且不发起学校登录；锁空闲时返回 `SUCCESS` 并刷新会话；无保存凭据返回 `FAILED`。
- [x] 编译与测试：`sh gradlew :data:compileDebugKotlin`、`sh gradlew testDebugUnitTest`、`sh gradlew assembleDebug` 全部通过。

---

# 当前任务：会话续期功能真机回归验证

上一里程碑（学校 Cookie 过期自动退出登录修复 + 登录界面转圈死锁修复）的代码与单元测试已完成，以下行为需在真机上逐一验证。任一项失败即按相应模块回溯定位。

- [ ] 已登录用户会话过期后进入登录界面：直接落到登录表单，不再持续转圈（死锁主场景回归）。
- [ ] 未登录用户进入登录界面：正常落到登录表单。
- [ ] 锁空闲时的 401 场景：自动静默刷新并重试一次仍有效。
- [ ] 使用空教室 / 刷新课表等学校功能时自动静默重登成功，不退出登录、不弹错。
- [ ] 学校要求短信二次验证时，自动刷新弹出验证码弹窗，输入验证码后刷新继续、操作重试成功。
- [ ] 取消验证码弹窗后不退出登录：登录态置为未登录但保留学号密码，重登无需重新输入。
- [ ] 网络不可用时学校操作失败但不退出登录、不清除已存凭据。
- [ ] 手动在设置页点击"检查登录状态"正常（成功/失败提示符合新逻辑）。
- [ ] 显式注销仍彻底清除所有登录态与凭据。

## 不在本次范围内

- 图形验证码识别与输入 UI（保持当前"需重新登录"的通路）。
- 应用启动 / 进入前台时的主动会话校验（当前仅按需刷新）。
- 将 `loginMutex` 改为可重入锁（`ReentrantLock`）等更大范围的并发重构。
- 重写 Cookie 存储加密方案。
- 修改 BIT101 后端注册、校验或 `fake-cookie` 协议。
- 与本次会话续期/死锁修复无关的 API / 仓库 / UI 重构。
