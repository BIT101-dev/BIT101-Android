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
- [x] 保留 Java 17、`jvmTarget = 17` 和 `targetSdk = 34`；按 BIT-Login Android AAR metadata 要求将 `compileSdk` 从 34 升级到 36，不改变运行时 target 行为。
- [x] 检查 Kotlin 2.4 对现有 `kotlinOptions` DSL 的兼容性；当前仍可用，无需扩大配置改写。
- [x] 运行编译任务完成 Gradle configuration，确认所有插件均可解析。
- [x] 编译 `api` 模块，确认项目能够读取 BIT-Login v4.0.0 的 Kotlin metadata。
- [x] 编译使用 KSP 的 `config` 和 `data` 模块，确认 Hilt 2.57.2、Room 2.8.4 与 KSP 2.3.10 兼容；Hilt 和 Room 分别由 2.48、2.6.1 升级以支持 KSP2，同时保持 AGP 8 兼容。
- [x] 将 Hilt 处理器使用的 `kotlin-metadata-jvm` 固定为 2.4.10，使 Hilt 2.57.2 的聚合 Javac 阶段可读取 Kotlin 2.4 metadata，同时避免升级到要求 AGP 9 的 Hilt 版本。
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

- [x] 新增职责单一的 `SchoolLoginService`。
- [x] 接口只暴露应用需要的登录操作，不暴露 BIT-Login 的 `BaseLogin`、`HttpClient` 或 `JsonObject`。
- [x] 定义应用侧 Cookie 数据模型，包含以下字段：
  - `name`
  - `value`
  - `domain`
  - `path`
  - `secure`
  - `expiresEpochSeconds`
- [x] 登录方法接收 `username` 和 `password`，返回完整 Cookie 列表；成功但没有任何可用 Cookie 时视为登录失败。
- [x] 使用 `JxzxehallLogin` 创建教学中心登录器，行为等价于 `BitLogin.jxzxehallLogin()`，同时保留底层 SSO 引用以可靠关闭会话。
- [x] 调用登录器的 `login(username, password)` 完成统一身份认证和教学中心回调。
- [x] 通过 `getSession().cookieDetails()` 获取完整 Cookie，而不是使用会丢失 domain/path 信息的 `cookieMap()` 或 Cookie 字符串。
- [x] 把 SDK `CookieDetail` 映射为应用侧 Cookie 数据模型后再返回。
- [x] 使用 `try/finally` 确保登录成功、失败及 WebVPN 替换会话后都关闭 SDK `HttpClient`。
- [x] 适配层不记录用户名、密码、Cookie 值、ticket、验证码或短信验证码。
- [x] 适配层不缓存明文密码，也不引入新的持久化机制。
- [x] 不拦截 SDK 登录异常，保留原始类型、消息和 cause。
- [x] 应用侧新增异常不依赖上游 HTML 文本或页面元素。

### 登录适配层异常策略

- [x] `LoginError` 作为登录失败向上抛出，不转换为成功值或空 Cookie。
- [x] `CaptchaError` 暂时向上抛出；后续验证码 UI 可在独立任务中增加。
- [x] `SmsVerificationError` 暂时向上抛出。
- [x] 网络 I/O、超时和上游页面变化异常保留 cause 后向上抛出。
- [x] 适配层不使用宽泛 `catch (Exception) { false }` 吞掉诊断信息。

## 3. 将 SDK Cookie 桥接到现有 CookieManager

现有校园 Retrofit 服务依赖 `LoginStatus.cookieManager` 和 `JavaNetCookieJar`。BIT-Login 使用独立的 Ktor CookieStorage，因此必须显式同步 Cookie。

- [x] 在 `data` 模块增加从登录适配层 Cookie 数据到 `java.net.HttpCookie` 的转换逻辑。
- [x] 保留 Cookie 的 `name` 和 `value`。
- [x] 保留 Cookie 的 `domain`，并正确处理带前导点和不带前导点的 domain。
- [x] domain 为空时拒绝写入，不把无 domain Cookie 写给所有校园域名。
- [x] 保留 Cookie 的 `path`；空 path 规范化为 `/`。
- [x] 保留 Cookie 的 `secure` 属性。
- [x] 将 SDK 的秒级绝对过期时间转换为 `HttpCookie.maxAge` 所需的剩余秒数。
- [x] 会话 Cookie 的 `expiresEpochSeconds == null` 保持为会话 Cookie，不伪造长期有效期。
- [x] 已过期 Cookie 不写入 CookieStore；同步新会话时清除旧学校 Cookie。
- [x] 使用与 Cookie domain 和 secure 属性匹配的 URI 调用 `cookieStore.add(uri, cookie)`。
- [x] 同步一批新登录 Cookie 前，清除 `bit.edu.cn` 范围内的旧学校认证 Cookie，避免新旧会话混用。
- [x] Cookie 桥接不接触 `fake-cookie` 设置项或非学校域 Cookie。
- [ ] 使用真实 `SharedPreferencesCookieStore` 完成进程级持久化手工验证。
- [x] 所有 Cookie 在修改 CookieStore 前完成转换和校验；后续仓库仅在同步成功后标记登录成功。

### Cookie 桥接测试

- [x] 测试普通 HTTPS Cookie 的 name、value、domain、path 和 secure 转换。
- [x] 测试带前导点 domain 的 Cookie。
- [x] 测试空 domain Cookie 被拒绝。
- [x] 测试会话 Cookie 不被错误设置为立即过期。
- [x] 测试未来绝对过期时间正确转换为剩余 `maxAge`。
- [x] 测试已过期 Cookie 不写入 CookieStore。
- [x] 测试同名但 path 不同的 Cookie 不会互相覆盖。
- [x] 测试同步登录 Cookie 会替换旧学校会话，同时保留非学校域 Cookie。
- [x] 转换过程没有日志调用，不会输出 Cookie 值。

## 4. 替换 DefaultLoginRepo 的学校登录流程

修改 `data/src/main/java/cn/bit101/android/data/repo/DefaultLoginRepo.kt`，移除旧 CAS HTML 初始化、salt/execution 提取和本地加密提交逻辑。

- [x] 通过 `Bit101Api.schoolLogin` 向仓库提供新的 `SchoolLoginService` 等价适配边界。
- [x] 删除 `loginSchool()` 的 `salt` 和 `execution` 参数。
- [x] 删除学校登录中对 `api.schoolUser.initLogin()` 的调用。
- [x] 删除学校登录中对 `api.schoolUser.login()` 的调用。
- [x] 删除学校登录密码的 `AESUtils.encryptPassword()` 调用。
- [x] 删除旧 `captchaPayload` 生成逻辑及依赖旧漏洞行为的长注释。
- [x] 新 `loginSchool(username, password)` 调用 BIT-Login 适配层获取 Cookie 和网络模式。
- [x] 将 Cookie 同步到 `loginStatus.cookieManager` 并切换 Retrofit 网络模式后才返回成功。
- [x] 保留 `loginBIT101()` 中的 `AESUtils.encryptPassword()`，该流程仍依赖 BIT101 后端返回的 salt。
- [x] 保留 BIT101 后端 `webVpnVerifyInit`、`webVpnVerify`、`register` 和 `fake-cookie` 写入流程。
- [x] `login()` 仍先清理旧状态，再完成学校登录和 BIT101 后端登录。
- [x] 仅当学校登录、Cookie 同步和 BIT101 后端登录全部成功后，保存 SID、密码和 `status = true`。
- [x] 任一阶段失败时保持 `status = false`，不留下半登录状态。
- [x] 任一阶段失败时调用 `loginStatus.clear()`，清理学校 Cookie 和可能写入的 `fake-cookie`。
- [x] 不改变 SID 和密码当前通过 `EncryptedSharedPreferences` 保存的机制。

### checkLogin 行为

- [x] `checkBit101Login()` 继续通过 `api.user.check()` 验证 BIT101 后端会话。
- [x] `checkSchoolLogin()` 不再通过解析 `/cas/login` HTML 判断是否已登录。
- [x] 使用教学中心 app config 和当前学期接口检查持久 Cookie，避免会话有效时重复提交密码。
- [x] 如果学校 Cookie 有效，直接返回成功。
- [x] 如果学校 Cookie 失效且已保存 SID/密码，通过 BIT-Login 重新登录并覆盖学校 Cookie。
- [x] 如果学校 Cookie 失效且没有保存凭据，返回失败。
- [x] `checkLogin()` 任一侧失败时执行统一清理，保持现有“BIT101 后端和学校会话均有效才算登录”的语义。
- [x] 保留现有 `checkLogin()` 失败即清理的产品语义；业务重试入口已排除取消、参数和数据库异常，不对这些错误触发认证检查。

### doOperationRequiresLogin 行为

- [x] 保持业务操作首次失败后检查登录并最多重试一次的整体行为。
- [x] 取消、参数和数据库异常直接向上传递，不触发登录请求。
- [x] 登录恢复失败时以原始业务异常作为 cause。
- [x] 一次失败最多触发一次重新认证和一次业务重试，不循环登录。

### logout 行为

- [x] `logout()` 继续调用 `loginStatus.clear()`。
- [x] `LoginStatus.clear()` 会清除持久学校 Cookie、SID、密码、登录状态和 BIT101 `fake-cookie`。
- [x] SDK 会话在每次登录后关闭，不维护额外的全局 SDK logout 状态。
- [x] 使用互斥锁串行化登录和自动重新认证事务，避免并发覆盖账号状态。
- [x] 注销参与同一互斥事务，取消登录时在不可取消上下文完成清理。

## 5. 从 API 聚合对象移除旧 SchoolUser Retrofit 服务

- [x] 从 `api/src/main/java/cn/bit101/api/Bit101Api.kt` 删除 `SchoolUserApiService` import。
- [x] 从 `Bit101Api` 构造函数删除 `schoolLoginRetrofit` 参数。
- [x] 从 `Bit101Api` 删除 `schoolUser` 属性。
- [x] 从 `api/src/main/java/cn/bit101/api/Bit101ApiFactory.kt` 删除学校登录 Retrofit 的构建代码。
- [x] 在 `Bit101Api` 中以明确命名的 `schoolLogin` 属性暴露无状态适配层。
- [x] 保留 `jxzxehallappRetrofit` 和 `lexueRetrofit`。
- [x] 保留当前业务服务继续使用 `option.schoolClient`，确保它们读取同步后的持久 Cookie。
- [x] 未处理与本次任务无关的 `jwmsRetrofit`、`jwcRetrofit` 清理。

## 6. 清理旧统一认证实现

- [x] 删除 `api/src/main/java/cn/bit101/api/service/school/SchoolUserApiService.kt`。
- [x] 删除 `api/src/main/java/cn/bit101/api/model/http/school/SchoolUser.kt`。
- [x] 删除 `api/src/main/java/cn/bit101/api/converter/login/GetSchoolInitLoginConvertFactory.kt`。
- [x] 删除 `api/src/main/java/cn/bit101/api/converter/login/PostSchoolLoginConvertFactory.kt`。
- [x] 从 `api/src/main/java/cn/bit101/api/converter/ConverterFactory.kt` 删除旧登录 model import。
- [x] 从 `ConverterFactory` 删除两个旧登录 converter import。
- [x] 从 `ConverterFactory` 的 converter map 删除旧登录响应类型注册。
- [x] `api/converter/login/` 已清空，不会保留在 Git 中。
- [x] 搜索并移除所有 `schoolUser` 引用。
- [x] 搜索并移除所有 `SchoolUserApiService` 引用。
- [x] 搜索并移除所有 `GetSchoolInitLoginDataModel` 引用。
- [x] 搜索并移除所有 `PostSchoolLoginDataModel` 引用。
- [x] 搜索并移除所有 `GetCheckNeedCaptchaDataModel` 引用。
- [x] `AESUtils` 仍被 BIT101 后端登录使用，本次未删除。

## 7. 清理不再使用的学校登录 URL 配置

只有确认没有其他 Retrofit 或业务代码使用这些字段后再删除。

- [x] 从 `ApiUrlOption` 删除 `schoolLoginUrl`。
- [x] 从本地、WebVPN 和开发环境 URL 配置中删除对应 `schoolLoginUrl` 参数。
- [x] 调整所有 `ApiUrlOption` 构造调用，字段均使用命名参数。
- [x] 保留教学中心和乐学 URL。
- [x] 保留 BIT101 后端仍使用的 WebVPN 相关 URL。
- [x] BIT-Login 通过 `NetworkEnv` 和 `Config.Urls.active` 管理登录 URL，应用不再注入旧 `schoolLoginUrl`。

## 8. Hilt 和依赖边界

- [x] 登录适配层由 `Bit101Api` 创建并持有。
- [x] 适配层无状态，直接由 `Bit101Api` 持有，无需增加 Hilt 绑定。
- [x] 未增加不必要的 Hilt 模块或绑定。
- [x] 不把 BIT-Login `BaseLogin` 或 `HttpClient` 注册成全局单例。
- [x] ViewModel 和 feature 模块不直接依赖 `cn.bit101.bitlogin`。
- [x] 保持 `LoginRepo` 作为 UI 的登录边界。
- [x] BIT-Login 保持为 `api` 模块内部 implementation 依赖，对外只暴露本项目 DTO。

## 9. 单元测试和集成验证

### 无真实账号测试

- [x] 为 Cookie 映射和写入逻辑增加 JVM 单元测试。
- [x] 为登录成功后才写入最终状态增加测试。
- [x] 为学校登录失败时清理半登录状态增加测试。
- [x] 为 BIT101 后端登录失败时清理已同步学校 Cookie 增加测试。
- [x] 为持久 Cookie 有效时不重复调用 BIT-Login 增加测试。
- [x] 为持久 Cookie 失效且保存了凭据时重新登录增加测试。
- [x] 为缺少保存凭据时不尝试空账号登录增加测试。
- [x] 为 `doOperationRequiresLogin` 最多重试一次增加测试。
- [x] 测试注销清理 SID、密码、状态、Cookie 和 `fake-cookie`。
- [x] 静态检查生产代码和测试，未发现日志或异常拼接密码、Cookie、ticket 或验证码值。

### 编译与静态检查

- [x] 运行 `sh gradlew :api:compileKotlin`。
- [x] 运行 `sh gradlew :config:compileDebugKotlin`。
- [x] 运行 `sh gradlew :data:compileDebugKotlin`。
- [x] 运行 `sh gradlew testDebugUnitTest`，所有 Android 模块单元测试通过。
- [x] 运行 `sh gradlew assembleDebug`。
- [x] 搜索确认已删除符号没有残留引用。
- [x] Gradle dependency report 最终解析 Kotlin stdlib 2.4.10、Ktor 3.5.1 和 jsoup 1.17.2，无并存版本。
- [x] `:app:compileReleaseKotlin` 通过；当前 release 未启用 minify，BIT-Login、Ktor 和 kotlinx.serialization 不需要额外 keep rules。

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

- [x] 课程、考试、学期、空闲教室和乐学 DDL 的仓库公开接口没有变化。
- [x] feature 和 UI 模块无需感知 BIT-Login 类型。
- [x] 生产代码未把密码、Cookie、ticket 或验证码写入 Logcat。
- [x] SDK Cookie 使用 `cookieDetails()` 和应用 DTO 传递完整 domain/path 属性，未降级为 Cookie 字符串。
- [x] HTTPS Cookie 的 secure 属性由单元测试确认保留。
- [x] 秒级绝对过期时间到剩余 `maxAge` 的转换由单元测试覆盖，会话和过期 Cookie 行为正确。
- [x] 登录和 Cookie 同步位于 `Dispatchers.IO` 登录事务内，不阻塞主线程。
- [x] SDK Ktor 客户端在 `finally` 中关闭，包含 WebVPN 替换会话路径。
- [x] 登录、自动重认证和注销通过同一互斥锁串行化，避免账号 Cookie 交叉覆盖。
- [x] 登录失败只调用 `LoginStatus.clear()`，不清理非认证设置。
- [x] 保留现有 BIT101 `401` 触发 `LoginStatus.clear()` 的产品语义。

## 11. 文档与收尾

- [x] 更新登录实现文档，移除旧 CAS salt/execution 和绕过验证码的说明；BIT101 后端模型中的同名字段按协议保留。
- [x] README 记录学校登录由 BIT-Login 提供、业务请求仍由 Retrofit 提供的边界。
- [x] 本 TODO 的范围说明记录 BIT-Login v4.0.0 尚未覆盖的能力，避免误删现有校园业务接口。
- [x] README 和本 TODO 记录当前验证码和短信验证尚未接入 UI 的限制。
- [x] README 记录 Kotlin 2.4.10、Compose Compiler 插件、KSP、AGP、Gradle、Hilt 和 Room 的最终版本组合。
- [x] 功能提交未混入 IDE 文件或与迁移无关的批量格式化改动。
- [x] 自动测试、Debug 构建和 release Kotlin 编译通过，迁移未添加临时诊断代码。

## 完成标准

满足以下全部条件后，本迁移才算完成：

- [x] 项目使用与 BIT-Login v4.0.0 兼容的 Kotlin 2.4.10 工具链并可完整构建。
- [x] 学校统一身份认证不再调用 `SchoolUserApiService` 或解析 CAS 登录 HTML。
- [x] 登录由 BIT-Login 完成，SDK Cookie 以完整属性同步到持久 CookieStore。
- [x] 旧学校登录 Retrofit service、DTO 和 converter 已删除且无残留引用。
- [x] BIT101 后端登录和 `fake-cookie` 流程保留，并由仓库事务测试覆盖。
- [ ] 课程、考试、学期、空闲教室和乐学 DDL 无功能回退。
- [ ] 登录失败、Cookie 失效、进程重启和注销路径行为正确。
- [x] SDK 客户端无已知资源泄漏，日志无敏感信息泄漏。
- [x] 单元测试、模块编译、release Kotlin 编译和 `assembleDebug` 全部通过。
