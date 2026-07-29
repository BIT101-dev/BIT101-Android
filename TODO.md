# BIT-Login 登录迁移 TODO

## 目标

使用已经引入的 `BIT-Login v4.0.0` 替换应用当前自行实现的北京理工大学统一身份认证登录流程，同时保持课程、考试、学期、空闲教室和乐学 DDL 等现有功能可用。

本次迁移只替换登录能力，不直接用 BIT-Login 重写全部校园业务接口。BIT-Login v4.0.0 尚未覆盖乐学日历、考试安排、完整学期列表、校区列表和教学楼列表，删除这些现有接口会造成功能回退。

## 最终架构

- BIT-Login 负责统一身份认证、服务回调、校内网络或 WebVPN 环境识别，以及教学中心登录初始化。
- `SchoolJxzxehallappApiService` 继续负责课程、考试、学期和空闲教室相关请求。
- `SchoolLexueApiService` 继续负责乐学日历订阅地址和 ICS 数据请求。
- BIT-Login 登录成功后，将其 Ktor 会话中的 Cookie 完整同步到现有 `LoginStatus.cookieManager`。
- 现有 Retrofit/OkHttp 校园接口继续通过 `JavaNetCookieJar` 使用同步后的 Cookie。
- BIT101 后端登录、`fake-cookie` 和 `webVpnVerify` 流程保持不变。
- 应用不长期持有 BIT-Login 的 Ktor 客户端；同步 Cookie 后及时关闭会话。
- 进程重启后优先使用现有持久 Cookie；Cookie 失效时使用加密存储的学号和密码重新执行 BIT-Login 登录。

## 不在本次范围内

- 不使用 BIT-Login 的 `Course` 重写现有课程仓库。
- 不使用 BIT-Login 的 `Classroom` 重写现有空闲教室仓库。
- 不移除 `SchoolJxzxehallappApiService`。
- 不移除 `SchoolLexueApiService`。
- 不下线考试、学期选择、校区/教学楼选择或乐学 DDL 功能。
- 不改变 Room 数据库结构和现有业务实体。
- 不改变 BIT101 后端注册、校验和 `fake-cookie` 协议。
- 本次不新增验证码图片或短信验证码交互界面；相关 SDK 异常先通过现有失败通路向上传递。
- 不顺带重构与登录迁移无关的 API、仓库或 UI。

## 1. 升级 Kotlin 和 Android 构建工具链

BIT-Login v4.0.0 使用 Kotlin 2.4.10 发布，当前项目仍使用 Kotlin 1.9.22。必须先统一编译工具链，避免 Kotlin metadata 不兼容。

- [x] 将根目录 `build.gradle` 中的 `org.jetbrains.kotlin.android` 从 `1.9.22` 升级到 `2.4.10`。
- [x] 将根目录 `build.gradle` 中的 `org.jetbrains.kotlin.jvm` 从 `1.9.22` 升级到 `2.4.10`。
- [x] 将 `com.google.devtools.ksp` 从 `1.9.22-1.0.17` 升级到 `2.3.10`。
- [x] 将 Android Gradle Plugin 从 `8.1.2` 升级到 `8.13.2`；该版本同时满足 Kotlin 2.4 class files 和 KSP 2.3.10 的 Android Variant API 要求。
- [x] 将 `gradle/wrapper/gradle-wrapper.properties` 中的 Gradle Wrapper 从 `8.0` 升级到 AGP 8.13.2 要求的 Gradle 8.13。
- [x] 在根目录插件声明中添加 `org.jetbrains.kotlin.plugin.compose`，版本与 Kotlin 保持一致为 `2.4.10`。
- [x] 在所有启用 Compose 的 Android 模块中应用 `org.jetbrains.kotlin.plugin.compose`。
- [x] 删除根目录 `versions.kotlinCompilerExtensionVersion`，Kotlin 2.x 不再使用旧的独立 Compose Compiler 版本配置。
- [x] 删除各 Compose 模块中的 `composeOptions { kotlinCompilerExtensionVersion ... }`。
- [x] 保留现有 Java 17、`jvmTarget = 17`、`compileSdk = 34` 和 `targetSdk = 34`。
- [x] 检查 Kotlin 2.4 对现有 `kotlinOptions` DSL 的兼容性；当前仍可用，无需扩大配置改写。
- [x] 运行编译任务完成 Gradle configuration，确认所有插件均可解析。
- [x] 编译 `api` 模块，确认项目能够读取 BIT-Login v4.0.0 的 Kotlin metadata。
- [x] 编译使用 KSP 的 `config` 和 `data` 模块，确认 Hilt 2.57.2、Room 2.8.4 与 KSP 2.3.10 兼容；Hilt 和 Room 分别由 2.48、2.6.1 升级以支持 KSP2，同时保持 AGP 8 兼容。
- [x] 仅升级实际不兼容的 Hilt 和 Room 依赖及处理器，未批量升级其他库。

### 工具链阶段验收

- [x] `sh gradlew :api:compileKotlin` 成功（使用项目要求的 Java 17）。
- [x] `sh gradlew :config:compileDebugKotlin` 成功（使用项目要求的 Java 17）。
- [x] `sh gradlew :data:compileDebugKotlin` 成功（使用项目要求的 Java 17）。
- [x] `:features:common:compileDebugKotlin` 成功。
- [x] 构建日志中不存在 Kotlin metadata version mismatch。
- [x] 构建日志中不存在旧 Compose Compiler 与 Kotlin 2.4 不兼容错误。

## 2. 定义 BIT-Login 登录适配层

适配层放在 `api` 模块，避免 `data` 模块直接依赖 BIT-Login 的具体登录器、Ktor 客户端和 JSON 返回结构。

- [ ] 新增一个职责单一的校园登录接口，例如 `SchoolLoginService`。
- [ ] 接口只暴露应用需要的登录操作，不暴露 BIT-Login 的 `BaseLogin`、`HttpClient` 或 `JsonObject`。
- [ ] 定义应用侧 Cookie 数据模型，至少包含以下字段：
  - `name`
  - `value`
  - `domain`
  - `path`
  - `secure`
  - `expiresEpochSeconds`
- [ ] 登录方法接收 `username` 和 `password`，返回完整 Cookie 列表；成功但没有任何可用 Cookie 时视为登录失败。
- [ ] 实现中调用 `BitLogin.jxzxehallLogin()` 创建新的教学中心登录器。
- [ ] 调用登录器的 `login(username, password)` 完成统一身份认证和教学中心回调。
- [ ] 通过 `getSession().cookieDetails()` 获取完整 Cookie，而不是使用会丢失 domain/path 信息的 `cookieMap()` 或 Cookie 字符串。
- [ ] 把 SDK `CookieDetail` 映射为应用侧 Cookie 数据模型后再返回。
- [ ] 使用 `try/finally` 确保登录成功或失败后都关闭 SDK `HttpClient`。
- [ ] 不在日志、异常消息或调试输出中记录用户名、密码、Cookie 值、ticket、验证码或短信验证码。
- [ ] 不缓存明文密码，不在适配层引入新的持久化机制。
- [ ] 保持 SDK 原始异常的 cause，便于区分凭据错误、验证码要求、短信验证要求、网络错误和服务结构变化。
- [ ] 对外异常信息避免依赖上游中文 HTML 文本或页面元素。

### 登录适配层异常策略

- [ ] `LoginError` 作为登录失败向上抛出，不转换为成功值或空 Cookie。
- [ ] `CaptchaError` 暂时向上抛出，由现有登录 UI 显示通用失败；后续验证码 UI 可在独立任务中增加。
- [ ] `SmsVerificationError` 暂时向上抛出，由现有登录 UI 显示通用失败。
- [ ] 网络 I/O、超时和上游页面变化异常保留 cause 后向上抛出。
- [ ] 不使用宽泛 `catch (Exception) { false }` 吞掉诊断信息。

## 3. 将 SDK Cookie 桥接到现有 CookieManager

现有校园 Retrofit 服务依赖 `LoginStatus.cookieManager` 和 `JavaNetCookieJar`。BIT-Login 使用独立的 Ktor CookieStorage，因此必须显式同步 Cookie。

- [ ] 在 `data` 模块增加从登录适配层 Cookie 数据到 `java.net.HttpCookie` 的转换逻辑。
- [ ] 保留 Cookie 的 `name` 和 `value`。
- [ ] 保留 Cookie 的 `domain`，并正确处理带前导点和不带前导点的 domain。
- [ ] domain 为空时拒绝写入或使用明确的来源主机；不得把无 domain Cookie 无差别写给所有校园域名。
- [ ] 保留 Cookie 的 `path`；空 path 规范化为 `/`。
- [ ] 保留 Cookie 的 `secure` 属性。
- [ ] 将 SDK 的秒级绝对过期时间转换为 `HttpCookie.maxAge` 所需的剩余秒数。
- [ ] 会话 Cookie 的 `expiresEpochSeconds == null` 保持为会话 Cookie，不伪造长期有效期。
- [ ] 已过期 Cookie 不写入 CookieStore，并清除同 name/domain/path 的旧值。
- [ ] 使用与 Cookie domain 和 secure 属性匹配的 URI 调用 `cookieStore.add(uri, cookie)`。
- [ ] 同步一批新登录 Cookie 前，清除旧的学校认证 Cookie，避免新旧会话混用。
- [ ] 不清除刚由 `loginBIT101()` 写入的 `fake-cookie` 设置项。
- [ ] 确认 `SharedPreferencesCookieStore` 能持久化转换后的 domain、path、secure 和 maxAge。
- [ ] Cookie 同步作为一次登录事务处理；转换或写入任何关键 Cookie 失败时，登录不得被标记为成功。

### Cookie 桥接测试

- [ ] 测试普通 HTTPS Cookie 的 name、value、domain、path 和 secure 转换。
- [ ] 测试带前导点 domain 的 Cookie。
- [ ] 测试 host-only 或空 domain Cookie 的处理策略。
- [ ] 测试会话 Cookie 不被错误设置为立即过期。
- [ ] 测试未来绝对过期时间正确转换为剩余 `maxAge`。
- [ ] 测试已过期 Cookie 不会残留在 CookieStore。
- [ ] 测试同名但 domain/path 不同的 Cookie 不会互相覆盖。
- [ ] 测试重复登录会替换旧会话，而不是累积冲突 Cookie。
- [ ] 测试转换过程不输出 Cookie 值到日志。

## 4. 替换 DefaultLoginRepo 的学校登录流程

修改 `data/src/main/java/cn/bit101/android/data/repo/DefaultLoginRepo.kt`，移除旧 CAS HTML 初始化、salt/execution 提取和本地加密提交逻辑。

- [ ] 通过构造函数注入新的 `SchoolLoginService` 或等价适配层。
- [ ] 删除 `loginSchool()` 的 `salt` 和 `execution` 参数。
- [ ] 删除学校登录中对 `api.schoolUser.initLogin()` 的调用。
- [ ] 删除学校登录中对 `api.schoolUser.login()` 的调用。
- [ ] 删除学校登录密码的 `AESUtils.encryptPassword()` 调用。
- [ ] 删除旧 `captchaPayload` 生成逻辑及依赖旧漏洞行为的长注释。
- [ ] 新 `loginSchool(username, password)` 调用 BIT-Login 适配层获取 Cookie。
- [ ] 将 Cookie 同步到 `loginStatus.cookieManager` 后才返回成功。
- [ ] 保留 `loginBIT101()` 中的 `AESUtils.encryptPassword()`，该流程仍依赖 BIT101 后端返回的 salt。
- [ ] 保留 BIT101 后端 `webVpnVerifyInit`、`webVpnVerify`、`register` 和 `fake-cookie` 写入流程。
- [ ] 确认 `login()` 仍先清理旧状态，再完成学校登录和 BIT101 后端登录。
- [ ] 仅当学校登录、Cookie 同步和 BIT101 后端登录全部成功后，保存 SID、密码和 `status = true`。
- [ ] 任一阶段失败时保持 `status = false`，不留下半登录状态。
- [ ] 任一阶段失败时清理新写入的学校 Cookie 和可能写入的 `fake-cookie`。
- [ ] 不改变 SID 和密码当前通过 `EncryptedSharedPreferences` 保存的机制。

### checkLogin 行为

- [ ] `checkBit101Login()` 继续通过 `api.user.check()` 验证 BIT101 后端会话。
- [ ] `checkSchoolLogin()` 不再通过解析 `/cas/login` HTML 判断是否已登录。
- [ ] 优先使用一个低成本、确实需要教学中心认证的现有接口检查持久 Cookie 是否有效，避免每次检查都重新提交密码。
- [ ] 如果学校 Cookie 有效，直接返回成功。
- [ ] 如果学校 Cookie 失效且已保存 SID/密码，通过 BIT-Login 重新登录并覆盖学校 Cookie。
- [ ] 如果学校 Cookie 失效且没有保存凭据，返回失败。
- [ ] `checkLogin()` 任一侧失败时执行统一清理，保持现有“BIT101 后端和学校会话均有效才算登录”的语义。
- [ ] 区分“认证失效”和普通业务接口异常；服务临时不可用时不要误删用户凭据，除非确认现有产品语义要求如此。

### doOperationRequiresLogin 行为

- [ ] 保持业务操作首次失败后检查登录并最多重试一次的整体行为。
- [ ] 避免对所有异常都无条件重新登录；至少不要因本地解析、数据库或参数错误触发登录请求。
- [ ] 登录恢复失败时保留原始异常或登录异常作为 cause，而不是只抛出无上下文的 `Exception("check login failed")`。
- [ ] 确认一次失败最多触发一次重新认证和一次业务重试，避免循环登录。

### logout 行为

- [ ] `logout()` 继续调用 `loginStatus.clear()`。
- [ ] 确认注销会清除持久学校 Cookie、SID、密码、登录状态和 BIT101 `fake-cookie`。
- [ ] 由于 SDK 会话在每次登录后已关闭，不需要维护额外的全局 SDK logout 状态。

## 5. 从 API 聚合对象移除旧 SchoolUser Retrofit 服务

- [ ] 从 `api/src/main/java/cn/bit101/api/Bit101Api.kt` 删除 `SchoolUserApiService` import。
- [ ] 从 `Bit101Api` 构造函数删除 `schoolLoginRetrofit` 参数。
- [ ] 从 `Bit101Api` 删除 `schoolUser` 属性。
- [ ] 从 `api/src/main/java/cn/bit101/api/Bit101ApiFactory.kt` 删除学校登录 Retrofit 的构建代码。
- [ ] 如果新的登录适配层由 `Bit101Api` 暴露，在 `Bit101Api` 中提供明确命名的登录属性；否则通过 Hilt 单独提供，避免把它伪装成 Retrofit 服务。
- [ ] 保留 `jxzxehallappRetrofit` 和 `lexueRetrofit`。
- [ ] 保留当前业务服务继续使用 `option.schoolClient`，确保它们读取同步后的持久 Cookie。
- [ ] 不处理与本次任务无关的 `jwmsRetrofit`、`jwcRetrofit` 清理，除非它们因构造函数调整导致编译问题。

## 6. 清理旧统一认证实现

- [ ] 删除 `api/src/main/java/cn/bit101/api/service/school/SchoolUserApiService.kt`。
- [ ] 删除 `api/src/main/java/cn/bit101/api/model/http/school/SchoolUser.kt`。
- [ ] 删除 `api/src/main/java/cn/bit101/api/converter/login/GetSchoolInitLoginConvertFactory.kt`。
- [ ] 删除 `api/src/main/java/cn/bit101/api/converter/login/PostSchoolLoginConvertFactory.kt`。
- [ ] 从 `api/src/main/java/cn/bit101/api/converter/ConverterFactory.kt` 删除旧登录 model import。
- [ ] 从 `ConverterFactory` 删除两个旧登录 converter import。
- [ ] 从 `ConverterFactory` 的 converter map 删除旧登录响应类型注册。
- [ ] 如果 `api/converter/login/` 清空，删除空目录。
- [ ] 搜索并移除所有 `schoolUser` 引用。
- [ ] 搜索并移除所有 `SchoolUserApiService` 引用。
- [ ] 搜索并移除所有 `GetSchoolInitLoginDataModel` 引用。
- [ ] 搜索并移除所有 `PostSchoolLoginDataModel` 引用。
- [ ] 搜索并移除所有 `GetCheckNeedCaptchaDataModel` 引用。
- [ ] 确认 `AESUtils` 仍被 BIT101 后端登录使用，因此本次不得删除。

## 7. 清理不再使用的学校登录 URL 配置

只有确认没有其他 Retrofit 或业务代码使用这些字段后再删除。

- [ ] 从 `ApiUrlOption` 删除 `schoolLoginUrl`。
- [ ] 从本地、WebVPN 和开发环境 URL 配置中删除对应 `schoolLoginUrl` 参数。
- [ ] 调整所有 `ApiUrlOption` 构造调用，保证字段顺序和命名参数正确。
- [ ] 保留教学中心和乐学 URL。
- [ ] 保留 BIT101 后端仍使用的 WebVPN 相关 URL。
- [ ] 确认 BIT-Login 自己通过 `NetworkEnv` 和 `Config.Urls.active` 管理其登录 URL，应用不再向 SDK 注入旧 `schoolLoginUrl`。

## 8. Hilt 和依赖边界

- [ ] 确定登录适配层的实例创建位置。
- [ ] 如果适配层无状态，使用 Hilt 单例或直接由 `Bit101Api` 持有。
- [ ] 如果通过 Hilt 提供，在 `NetModule` 或专用模块中绑定接口与实现。
- [ ] 不把 BIT-Login `BaseLogin` 或 `HttpClient` 注册成全局单例。
- [ ] 不让 ViewModel 或 feature 模块直接依赖 `cn.bit101.bitlogin`。
- [ ] 保持 `LoginRepo` 作为 UI 的登录边界，除非验证码/SMS UI 在后续任务中单独设计。
- [ ] 检查 `api/build.gradle` 中 BIT-Login 依赖的可见性；仅当 `data` 必须直接引用适配层返回类型时才调整为 `api` 依赖，优先让公开类型属于本项目包。

## 9. 单元测试和集成验证

### 无真实账号测试

- [ ] 为 Cookie 映射和写入逻辑增加 JVM 单元测试。
- [ ] 为登录成功后才写入最终状态增加测试。
- [ ] 为学校登录失败时清理半登录状态增加测试。
- [ ] 为 BIT101 后端登录失败时清理已同步学校 Cookie 增加测试。
- [ ] 为持久 Cookie 有效时不重复调用 BIT-Login 增加测试。
- [ ] 为持久 Cookie 失效且保存了凭据时重新登录增加测试。
- [ ] 为缺少保存凭据时不尝试空账号登录增加测试。
- [ ] 为 `doOperationRequiresLogin` 最多重试一次增加测试。
- [ ] 测试注销清理 SID、密码、状态、Cookie 和 `fake-cookie`。
- [ ] 测试异常信息和日志不包含密码或 Cookie 值。

### 编译与静态检查

- [ ] 运行 `sh gradlew :api:compileKotlin`。
- [ ] 运行 `sh gradlew :config:compileDebugKotlin`。
- [ ] 运行 `sh gradlew :data:compileDebugKotlin`。
- [ ] 运行 `sh gradlew testDebugUnitTest`；如果根项目不存在该聚合任务，则运行各 Android 模块的 `testDebugUnitTest` 和 `:api:test`。
- [ ] 运行 `sh gradlew assembleDebug`。
- [ ] 搜索确认已删除符号没有残留引用。
- [ ] 检查 Gradle dependency report，确认只解析一个可用的 Kotlin stdlib、Ktor 和 jsoup 版本。
- [ ] 检查 release 构建或 R8 配置，确认 BIT-Login、Ktor 和 kotlinx.serialization 不需要额外 keep rules。

### 真实环境手工验证

- [ ] 使用正确学号和密码完成首次登录。
- [ ] 使用错误密码确认登录失败，且不会保存登录状态或残留 Cookie。
- [ ] 登录后刷新课程表。
- [ ] 登录后刷新考试安排。
- [ ] 登录后获取学期列表和当前学期。
- [ ] 登录后获取校区、教学楼和空闲教室。
- [ ] 登录后生成乐学日历订阅地址。
- [ ] 使用已有乐学订阅地址刷新 DDL。
- [ ] 杀死进程并重启，确认持久 Cookie 可以继续使用，或失效时能够使用保存凭据恢复。
- [ ] 清除或等待学校 Cookie 失效，确认自动重新登录成功。
- [ ] 注销后确认所有校园接口不再携带旧认证 Cookie。
- [ ] 分别在校园网和校外网络验证 BIT-Login 的网络环境选择及 WebVPN 登录。
- [ ] 遇到图形验证码或短信验证账号时，确认应用明确报告登录失败，不发生崩溃、死循环或静默成功。

## 10. 回归和安全检查

- [ ] 确认课程、考试、学期、空闲教室和乐学 DDL 的仓库公开接口没有变化。
- [ ] 确认 feature 和 UI 模块无需感知 BIT-Login 类型。
- [ ] 确认没有把密码、Cookie、ticket 或验证码写入 Logcat。
- [ ] 确认没有把 SDK Cookie 降级为不带 domain/path 的单一 Cookie 字符串。
- [ ] 确认 HTTPS Cookie 的 secure 属性没有丢失。
- [ ] 确认过期时间的毫秒/秒转换正确，没有生成永久 Cookie 或立即过期 Cookie。
- [ ] 确认登录和 Cookie 同步运行在 IO dispatcher 或由挂起网络 API 正确调度，不阻塞主线程。
- [ ] 确认 SDK Ktor 客户端在所有成功和失败路径中关闭。
- [ ] 确认并发登录不会交叉覆盖不同账号的 Cookie；必要时在登录仓库中串行化登录事务。
- [ ] 确认登录失败后的清理不会误删用户尚需保留的非认证设置。
- [ ] 确认现有 BIT101 `401` 清理逻辑仍符合产品预期。

## 11. 文档与收尾

- [ ] 更新与登录实现有关的代码注释，移除旧 CAS salt/execution 和绕过验证码的说明。
- [ ] 在开发文档中记录学校登录由 BIT-Login 提供、业务请求仍由 Retrofit 提供的边界。
- [ ] 记录 BIT-Login v4.0.0 尚未覆盖的能力，避免后续误删现有校园业务接口。
- [ ] 记录当前验证码和短信验证尚未接入 UI 的限制。
- [ ] 记录 Kotlin 2.4.10、Compose Compiler 插件、KSP、AGP 和 Gradle 的最终版本组合。
- [ ] 检查 `git diff`，确保没有混入 IDE 文件或与迁移无关的格式化改动。
- [ ] 所有自动测试、Debug 构建和关键手工回归通过后，再删除迁移过程中临时添加的诊断代码。

## 完成标准

满足以下全部条件后，本迁移才算完成：

- [ ] 项目使用与 BIT-Login v4.0.0 兼容的 Kotlin 2.4.10 工具链并可完整构建。
- [ ] 学校统一身份认证不再调用 `SchoolUserApiService` 或解析 CAS 登录 HTML。
- [ ] 登录由 BIT-Login 完成，SDK Cookie 以完整属性同步到持久 CookieStore。
- [ ] 旧学校登录 Retrofit service、DTO 和 converter 已删除且无残留引用。
- [ ] BIT101 后端登录和 `fake-cookie` 行为保持可用。
- [ ] 课程、考试、学期、空闲教室和乐学 DDL 无功能回退。
- [ ] 登录失败、Cookie 失效、进程重启和注销路径行为正确。
- [ ] SDK 客户端无已知资源泄漏，日志无敏感信息泄漏。
- [ ] 单元测试、模块编译和 `assembleDebug` 全部通过。
