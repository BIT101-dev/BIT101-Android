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

---

# 当前任务：学校 Cookie 过期导致 App 自动退出登录的修复

## 1. 背景与问题

用户反馈：学校统一身份认证 Cookie 大约在一天内过期，导致 App 在一天内自动退出登录（登录态被清除，需要重新输入学号密码）。

主要复现场景：使用课表 / 空教室等学校功能后出现退出登录。

## 2. 根因分析

学校 SSO 会话 Cookie 由服务端控制，有效期约为一天（无法从客户端延长）。Cookie 过期后 App 存在两条触发退出的路径，均直接执行 `LoginStatus.clear()`（连学号、密码、fakeCookie 一起抹掉）：

### 路径 A：`checkLogin()` 自动重登失败（主要场景）

```
学校 Cookie 过期
  → 课表/空教室等学校接口请求失败
  → doOperationRequiresLogin 捕获异常
  → checkLogin()
  → checkBit101Login() (user/check) 通过（fakeCookie 可能仍有效）
  → checkSchoolLogin() 校验 getCurrentTerm 失败
  → 用已存学号密码调用 loginSchool(sid, password, smsCodeHandler = null)
  → 学校要求短信二次验证时 SDK 抛出 SmsVerificationError
    （SsoLogin/BitSsoClient.completeSecondFactor 在 smsCodeCallback 为 null 时抛错）
  → checkLogin() 返回 false
  → loginStatus.clear() → 退出登录
```

问题点：
- `checkSchoolLogin()`（`data/.../repo/DefaultLoginRepo.kt`）重登时未传入短信验证码 handler，遇到二次验证直接失败。
- 自动重登只刷新学校 Cookie，不刷新 bit101 `fakeCookie`；若 fakeCookie 与学校会话联动过期，`checkBit101Login()` 也会失败。
- 任何失败（包括临时性网络错误）都触发 `loginStatus.clear()`，把可恢复的会话直接打成"未登录"。

### 路径 B：bit101 请求 401 拦截器

`data/.../net/DefaultAPIManager.kt` 中 bit101Client 拦截器给每个请求附加 `fake-cookie` 头，收到 HTTP 401 时立即 `loginStatus.clear()`，不做刷新与重试。

## 3. 修复目标

把"校验失败 → 直接登出"改为"校验失败 → 用已存学号密码静默刷新整个会话（学校 Cookie + bit101 fakeCookie）→ 学校要求短信验证时弹出验证码弹窗完成二次验证 → 刷新成功则重试原操作；只有确定性认证失败才登出"。

已确认的产品决策：
- 静默刷新遇到短信二次验证时：**直接弹出验证码输入弹窗**，输入后继续完成刷新。
- 刷新触发方式：**仅按需触发**（401 拦截器、checkLogin、doOperationRequiresLogin 失败时），不做应用启动时的主动校验。
- 主要修复目标：课表 / 空教室等学校功能场景（路径 A），同时覆盖 bit101 401 场景（路径 B）。

## 4. 总体方案

1. 数据层新增共享短信验证码总线 `SmsCodeRequestHub`，把验证码输入请求从任意页面/后台刷新统一上抛给全局弹窗。
2. `LoginRepo` 新增静默刷新能力 `refreshLogin()`，一次性刷新学校会话与 bit101 fakeCookie，返回明确的结果分类。
3. `checkLogin()` 改为"验证 → 刷新 → 分类处理"，仅确定性认证失败才清理；临时性失败不清除登录态。
4. `DefaultAPIManager` 401 拦截器改为"静默刷新 + 重试一次"，刷新失败才走原清理逻辑。
5. UI 层新增全局 `SmsCodeDialogHost`，复用现有 `VerificationCodeDialog`；登录页改为使用共享总线。
6. 补充单元测试覆盖刷新、自愈与失败分类。

## 5. 详细实施步骤

### 5.1 数据层：共享短信验证码总线（已完成）

- [x] 将 `PendingSmsCodeRequest` 从 `features/login/src/main/java/cn/bit101/android/features/login/LoginOrLogoutViewModel.kt` 移入 `data` 模块（`data/src/main/java/cn/bit101/android/data/repo/PendingSmsCodeRequest.kt`）。
  - 保持原有结构：`maskedPhone: String` + `CompletableDeferred<String>`，`submit(code)` 完成、`cancel()` 取消，并新增 `await()`。
  - `features/login` 中删除原类定义，改为从 `data` 模块 import。
- [x] 新增 Hilt 单例 `SmsCodeRequestHub`（`data/src/main/java/cn/bit101/android/data/common/SmsCodeRequestHub.kt`）：
  - `private val _pending = MutableStateFlow<PendingSmsCodeRequest?>(null)`
  - `val pending: StateFlow<PendingSmsCodeRequest?>`
  - `fun createSmsCodeHandler(): SmsCodeHandler`：回调里创建 `PendingSmsCodeRequest` → `_pending.value = it` → `deferred.await()` → `finally` 中 `_pending.value = null`。
  - 通过 `@Singleton` + `@Inject constructor()` 注册（与现有 `@Inject` 类一致），不需要新增 Module。
- [x] 确认 `data` 模块可访问 `cn.bit101.api.model.common.SmsCodeHandler/SmsCodeRequest`（通过 `api project(':api')` 传递依赖，已满足）。

### 5.2 数据层：`LoginRepo` 接口与结果分类（已完成）

- [x] 在 `data/src/main/java/cn/bit101/android/data/repo/base/LoginRepo.kt` 新增：
  - `enum class LoginRefreshResult { SUCCESS, NEEDS_INTERACTIVE, TRANSIENT, FAILED }`
  - `suspend fun refreshLogin(allowInteractive: Boolean = false): LoginRefreshResult`
- [x] 语义定义：
  - `SUCCESS`：学校 + bit101 会话均已刷新。
  - `NEEDS_INTERACTIVE`：需要用户交互（短信验证码被取消、图形验证码），会话未刷新。
  - `TRANSIENT`：临时性失败（网络 I/O、429、5xx），不应登出。
  - `FAILED`：确定性认证失败（密码错误、账号锁定、风控拒绝、无保存凭据）。

### 5.3 数据层：`DefaultLoginRepo` 实现静默刷新（已完成）

- [x] 构造注入 `SmsCodeRequestHub`。
- [x] 新增 `refreshLogin(allowInteractive)` 实现：
  - 进入 `loginMutex.withLock`。
  - 读取 `loginStatus.sid.get()` / `password.get()`，任一为空 → 返回 `FAILED`。
  - 组装 SMS handler：`allowInteractive` 为 true 时用 `hub.createSmsCodeHandler()`，否则传 null（纯静默）。
  - 依次调用 `loginSchool(sid, password, smsCodeHandler)` 与 `loginBIT101(sid, password)` 刷新学校 Cookie、webVpn 与 fakeCookie。
  - 全部成功 → `SUCCESS`。不修改 `status`（保持有效），不清除任何状态。
- [x] 异常分类（依据 BIT-Login SDK 类型，均来自 `cn.bit101.bitlogin`）：
  - `SmsVerificationError` / 短信回调被取消（`CancellationException`）/ `CaptchaError` → `NEEDS_INTERACTIVE`。
  - `LoginError` / `SsoHttpException`：`statusCode == 429` 或 `in 500..599` → `TRANSIENT`；其余 → `FAILED`。
  - 网络类（`IOException` 等）→ `TRANSIENT`。
  - 兜底 → `FAILED`（保守，不吞异常细节，保证可诊断）。
  - 分类遍历 cause 链，穿透 SDK 对异常的包装。
- [x] 重写 `checkLogin()`：
  - 保留 `checkBit101Login()`（`api.user.check().isSuccessful`）与 `checkSchoolLogin()`（`getCurrentTerm`）作为纯校验。
  - 重构 `checkSchoolLogin()`：**不再承担自动重登**，只做会话有效性校验（当前学期接口），把"重登"职责收敛到 `refreshLogin`。
  - 两侧均有效 → 返回 true。
  - 任一侧失效 → `refreshLogin(allowInteractive = true)`：
    - `SUCCESS` → 返回 true（登录态保持，`doOperationRequiresLogin` 重试原操作即可成功）。
    - `NEEDS_INTERACTIVE` → `loginStatus.status.set(false)`，**保留** sid/password/fakeCookie，返回 false（UI 显示登录入口，重登时已存凭据可用）。
    - `TRANSIENT` → 不清理登录态，返回 false。
    - `FAILED` → `loginStatus.clear()`，返回 false。
  - 移除现有"结果非 true 一律 `clear()`"的逻辑。
  - 通过私有 `refreshLoginLocked` 在 `checkLogin` 持有的锁内复用刷新逻辑，避免重复加锁死锁。
- [x] 保持 `login()`、`logout()`、`doOperationRequiresLogin()` 现有对外行为不变；`doOperationRequiresLogin` 自动受益于改进后的 `checkLogin`。

### 5.4 数据层：`DefaultAPIManager` 401 拦截器改为刷新 + 重试（已完成）

- [x] 构造注入 `javax.inject.Provider<LoginRepo>`，避免 `DefaultAPIManager ↔ DefaultLoginRepo` 的构造循环依赖（Dagger 标准做法）。
- [x] bit101Client 拦截器改造（`DefaultAPIManager.kt`）：
  - 请求头带重试标记（`X-BIT101-AUTH-RETRIED`）时不再重试，直接 `loginStatus.clear()` 并返回响应。
  - 收到 401 且未重试：
    - `runBlocking { loginRepoProvider.get().refreshLogin(allowInteractive = false) }`（纯静默，不在 OkHttp 线程上弹窗等待用户输入）。
    - `SUCCESS` → 重新读取 `loginStatus.fakeCookie.get()`，以 `fake-cookie` 头 + 重试标记重建请求，`proceed` 重试一次。
    - 其他结果 → 维持现状 `loginStatus.clear()` 并返回原 401 响应。
- [x] 保留现有 `runBlocking` 用法（文件中已有先例），确保重试不超过一次、无死循环。

### 5.5 UI 层：全局验证码弹窗宿主（已完成）

- [x] 新增 `features/common` 组件 `SmsCodeDialogHost`（`features/common/src/main/java/cn/bit101/android/features/common/component/dialog/SmsCodeDialogHost.kt`）：
  - 注入 `SmsCodeRequestHub`（通过 `MainViewModel` 暴露单例），`hub.pending.collectAsState()`。
  - 非空时渲染现有 `VerificationCodeDialog`：
    - `title = "输入短信验证码"`
    - `message = "学校统一身份认证要求二次验证，验证码已发送至 ${maskedPhone}"`
    - `onConfirm = pending::submit`，`onDismiss = pending::cancel`
- [x] 在 `features/src/main/java/cn/bit101/android/features/MainApp.kt` 中 `SnackbarHost` 旁挂载 `SmsCodeDialogHost`，使任意页面触发的刷新都能弹出验证码弹窗。
- [x] `features/login` 精简：
  - `LoginOrLogoutViewModel` 改用 `SmsCodeRequestHub.createSmsCodeHandler()`，删除本地 `pendingSmsCodeRequestLiveData` 与 `PendingSmsCodeRequest` 类。
  - `LoginOrLogoutScreen` 删除本地 `VerificationCodeDialog` 渲染（由全局宿主接管），仅保留登录/登出状态判断。

### 5.6 单元测试（已完成）

- [x] 扩展 `data/src/test/java/cn/bit101/android/data/repo/DefaultLoginRepoTest.kt`：
  - `refreshLogin` 同时刷新学校 Cookie 与 bit101 fakeCookie（stub `webVpnVerifyInit/webVpnVerify/register`）。
  - `checkLogin` 学校会话失效时通过 `refreshLogin` 自愈并返回 true。
  - 临时性失败（网络异常 `IOException`）不清理登录状态、不清除凭据。
  - 短信需求（`SmsVerificationError`）→ `NEEDS_INTERACTIVE`，保留 sid/password，status 置 false。
  - 密码错误（`LoginError` code 1030027）→ `FAILED`，触发 `clear()`。
  - 429（`SsoHttpException`）→ `TRANSIENT`，不清理凭据。
  - 无保存凭据 → `FAILED`，不发起登录请求。
- [x] 新增 `data/src/test/java/cn/bit101/android/data/common/SmsCodeRequestHubTest.kt`：覆盖 handler 发布/await、cancel 清理、已取消后 submit 被忽略。

## 6. 涉及文件清单

- `data/src/main/java/cn/bit101/android/data/repo/base/LoginRepo.kt`（新增 `LoginRefreshResult`、`refreshLogin`）
- `data/src/main/java/cn/bit101/android/data/repo/DefaultLoginRepo.kt`（`refreshLogin`、重写 `checkLogin`、`checkSchoolLogin` 收敛为纯校验、注入 hub）
- `data/src/main/java/cn/bit101/android/data/net/DefaultAPIManager.kt`（注入 `Provider<LoginRepo>`，401 刷新 + 重试一次）
- `data/src/main/java/cn/bit101/android/data/repo/PendingSmsCodeRequest.kt`（新增，自 `features/login` 移入）
- `data/src/main/java/cn/bit101/android/data/common/SmsCodeRequestHub.kt`（新增）
- `features/common/src/main/java/cn/bit101/android/features/common/component/dialog/SmsCodeDialogHost.kt`（新增）
- `features/src/main/java/cn/bit101/android/features/MainApp.kt`（挂载全局弹窗宿主）
- `features/src/main/java/cn/bit101/android/features/MainViewModel.kt`（暴露 `SmsCodeRequestHub` 单例）
- `features/login/src/main/java/cn/bit101/android/features/login/LoginOrLogoutViewModel.kt`（改用共享总线，删本地实现）
- `features/login/src/main/java/cn/bit101/android/features/login/LoginOrLogoutScreen.kt`（删除本地验证码弹窗）
- `api/build.gradle`（bit-login 依赖由 `implementation` 改为 `api`，供 data 模块访问 SDK 异常类型）
- `data/src/test/java/cn/bit101/android/data/repo/DefaultLoginRepoTest.kt`（新增测试用例）
- `data/src/test/java/cn/bit101/android/data/common/SmsCodeRequestHubTest.kt`（新增）

## 7. 验收标准

- [x] 编译与测试：`sh gradlew :data:compileDebugKotlin`、`sh gradlew testDebugUnitTest`、`sh gradlew assembleDebug` 全部通过。
- [ ] 登录后模拟/等待学校 Cookie 失效，使用空教室或刷新课表时自动静默重登成功，不退出登录、不弹错。（待真机验证）
- [ ] 学校要求短信二次验证时，自动刷新弹出验证码弹窗，输入验证码后刷新继续、操作重试成功。（待真机验证）
- [ ] 取消验证码弹窗后不退出登录：登录态置为未登录但保留学号密码，重登无需重新输入。（待真机验证）
- [ ] 网络不可用时学校操作失败但不退出登录、不清除已存凭据。（待真机验证）
- [ ] 手动在设置页点击"检查登录状态"仍正常（成功/失败提示符合新逻辑）。（待真机验证）
- [ ] bit101 会话失效（401）场景：自动静默刷新并重试成功；刷新失败时维持原登出行为。（待真机验证）
- [ ] 显式注销仍彻底清除所有登录态与凭据。（待真机验证）

## 8. 不在本次范围内

- [ ] 图形验证码识别与输入 UI（保持当前"需重新登录"的通路）。
- [ ] 应用启动 / 进入前台时的主动会话校验（本次仅按需刷新）。
- [ ] 重写 Cookie 存储加密方案。
- [ ] 修改 BIT101 后端注册、校验或 `fake-cookie` 协议。
- [ ] 与本次会话续期无关的 API / 仓库 / UI 重构。
