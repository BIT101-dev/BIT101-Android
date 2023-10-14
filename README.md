# BIT101-Android

* 网站主页：[android.bit101.cn](http://android.bit101.cn/)
* 主项目链接：[BIT101](https://github.com/flwfdd/BIT101)
* 服务端链接：[BIT101-GO](https://github.com/flwfdd/BIT101-GO)
* `API`文档：[API](https://j1dds2ogfu.apifox.cn/)

## 系统要求

安装运行`BIT101-Android`需要`Android 8.0`及以上，动态适配系统主题功能需要`Android 12`及以上。

## 支持功能

* 自动同步并显示课程表
* 自动从乐学拉取并显示日程
* 校园地图
* 包含`BIT101`网站全部功能
* 个性化显示效果配置

## 计划功能

* 手动添加日程
* 手动修改课程表
* 桌面小组建显示课程和日程
* 地图显示定位、导航功能

欢迎提出你的想法✨

---

本项目还用于大二下学期的`Android`课程结课作业，以下摘自提交的文档。

## 代码架构及实现

注：由于篇幅限制，服务端仅介绍和客户端直接相关的一些部分，对于服务端更详细的说明请见[GitHub](https://github.com/flwfdd/BIT101-GO)。

![架构](https://i.niupic.com/images/2023/05/22/b71Z.png)

项目整体使用`MVVM`的架构，有三层数据模型：底层`Model`层负责处理、提供数据，中间的`ViewModel`层负责对接数据并管理运行状态，顶层的`View`层使用模型层提供的数据和状态与用户进行交互。

具体地，底层的`Model`层主要使用了如下数据来源：
* `DataStore`：负责存储一些简单的变量、设置等
* `Room`：提供`SQLite`数据库访问，存储了课程、日程等
* `OkHttp`：提供与学校接口的网络交互，还使用了`Cookie Store`保存网络访问过程中的`Cookie`
* `Retrofit`：提供与`BIT101`服务端的网络交互
* `Jetpack Security`：系统级安全的存储方式，用于保存密码


中间的`ViewModel`层将数据层和视觉层隔离开，全部使用了响应式设计，尽可能使用`Flow`、`State`等进行数据传输，同时所有获取数据的操作都放到了协程里进行，以避免性能问题。

另外还用到了几个开源库用于处理数据：
* `Gson`：用于处理`JSON`格式的字符串
* `iCal4j`：用于解析从乐学获取到的`iCalendar`日历标准格式的字符串


顶层的`View`层为单`Activigy`架构，使用`Jetpack Compose`+`Material Design 3`实现，通过绑定`ViewModel`管理数据和状态。另外还用到了一些其他的开源组件：
* `Navigation Compose`：用于页面切换和管理导航
* `MapCompose`：用于显示地图
* `Accompanist Webview`：用于显示`BIT101`网页端
* `Coil`：用于显示图片


接下来将分模块介绍一些功能的实现。

## 登陆模块

### 学校接口和Cookie自动管理

学校网站的接口基本都是通过`Cookie`和各种各样的重定向来实现的，所有的其他页面都需要`login.bit.edu`的第三方认证，这个过程如果完全通过手动模拟完成将会非常痛苦，很难实现。在没有`RESTful API`的情况下，使用`Retrofit`这样高层次的框架反而不方便了，最好的方式就是完全模拟浏览器的行为。所以我的实现方式是使用`OkHttp`，并且通过加入一个[cookie-store](https://github.com/gotev/android-cookie-store)中间件实现`Cookie`的全自动管理，这样只需要像使用浏览器一样访问接口就可以了，权限验证将自动通过`Cookie`和重定向完成。

#### 统一身份认证流程

首先访问登录页面，获取头中的`Set-Cookie`、页面中`id`分别为`pwdEncryptSalt`和`execution`的`input`的标签的`value`（注意页面中可能有多个相同`id`的，需要的这俩被包裹在`id`为`pwdFromId`的`form`中），几次密码错误后需要获取验证码，获取验证码和登录时使用同一个`Cookie`即可。

然后在前端将明文密码和`pwdEncryptSalt`传入`EncryptPassword.js`计算出加密后的密码，再将`Cookie`、加密后的密码、`execution`、验证码（如果需要的话）通过`POST`发送到登陆页面，然后会经历一大堆乱七八糟的`302`重定向，注意这一大堆重定向过程中需要一直携带`Cookie`。

具体逻辑见 `AESUtils`

### 密码管理

由于需要实现自动重新登陆的功能，因此必须要保存学号和密码，但是直接保存这样的敏感信息显然不合适，因此我使用了`Jetpack`中的`Security`实现了密码的安全管理，这个库会使用的硬件加密的`KeyStore`生成和存储密钥，再用密钥配合`EncryptedSharedPreferences`存储数据，这样就可以保证用户帐号密码的安全。

然而美中不足的是，`cookie-store`是使用未加密的`SharedPreferences`存储`Cookie`的，这也会带来安全隐患，但由于时间限制，暂时还没有重写这部分的存储接口。

### BIT101登录

`BIT101`部分的接口使用`Retrofit`实现，登录时首先使用学号和统一身份认证密码与`BIT101`服务端交互换取一个`JWT`格式的`token`和数字组成`code`（该过程传输的密码服务端是无法解密的，只用于转发给学校服务器进行认证），然后用获取到的`token`和`code`向`BIT101`服务端发起登录请求，服务端会下发一个`fake-cookie`用于登录验证。如果没有注册过`BIT101`账号则会创建账号，默认密码设置为统一身份认证密码。

获取到`fake-cookie`后，需要在后续的请求中保持登录状态。由于`BIT101`接口的权限验证无需使用`Cookie`,而是在进行请求时使用一个自定义的`fake-cookie`头，因此建立了另一个`OkHttp`的`Client`，并添加了一个会自动在`Herder`里添加`fake-cookie`的中间件，然后再将这个`Client`设置给`Retrofit`即可。

`BIT101`网页端的`fake-cookie`是保存在`localStorage`里的，因此通过`Retrofit`登录获取到`fake-cookie`后，只需要在`WebView`启动时使用`JavaScript`代码将`fake-cookie`注入到`localStorage`即可。

## 课程和日程表模块

课程表会通过与学校教务系统等的后端进行交互，通过`login.bit.edu.cn`的重定向授权即可访问。日程表会通过乐学“导出日历”的功能获取订阅链接，之后通过订阅链接获取日程详情即可。这部分接口在[API文档](https://j1dds2ogfu.apifox.cn/)中均有比较详细的说明，在此就不过多赘述了。从学校接口获取到的课程和日程数据会通过`Room`存储在数据库内，再根据`DataStore`中的配置项（如学期、星期等）进行获取和显示。

### 左右滑动Tab实现

这部分是使用[Accompanist Pager](https://google.github.io/accompanist/pager/)库实现的，这个库现在已经并入了`Jetpack Compose`。

顶部左右滑动切换的`Indicator`指示块是基于[官方文档](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#TabRow(kotlin.Int,androidx.compose.ui.Modifier,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,kotlin.Function1,kotlin.Function0,kotlin.Function0))的例子进行修改而来的，添加了一些缩放效果使得移动时看起来像果冻一样，实现了非常好的动画效果，这样的动画完全自己写的话还是比较困难。

因为`Pager`内部是使用`LazyRow`实现的，所以混动切换到边界时会有阴影效果，不太美观。经过百般搜寻，才找到了去掉的办法——将组件使用一个`CompositionLocalProvider(LocalOverscrollConfiguration provides null){}`包裹，想要设置阴影效果的话定义一个`OverscrollConfiguration`并传入即可。



## 地图模块

### 数据来源

地图模块的数据来源于[OpenStreetMap](https://www.openstreetmap.org/)，这是一个开源的地图项目，北京理工大学网络开拓者协会的成员曾在上面绘制了详细的校园地图，还建立了一个在线地图网站[一点儿北理地图(A BIT of Map)](https://map.bitnp.net/)，非常感谢他们的贡献。

然而，`OpenStreetMap`在国内存在`DNS`污染的问题，客户端上往往无法正常访问，于是我先是在服务器上使用基于`Docker`的[openstreetmap-tile-server](https://github.com/Overv/openstreetmap-tile-server)搭建了地图瓦片服务，并部署了北京地区的地图，但是部署后发现占用服务器资源过大，很容易受到攻击，所以后来还是放弃了这个方案，使用服务器代理转发的方式来解决问题。

最终的解决方案是，先在服务器上修改`hosts`防止DNS污染，然后在`Nginx`上建立一个代理规则。当访问`https://map.bit101.flwfdd.xyz/tile/{z}/{x}/{y}.png`时，请求将被转发到`https://tile.openstreetmap.org/{z}/{x}/{y}.png`，虽然访问速度比较慢，但由于地图数据基本不会改变，所以可以在`Nginx`上加入缓存，学校地区的数据基本上就是用服务器本地的了。

另外注意，调用`OpenStreetMap`接口时需要传入`User-Agent`，否则会被拦截。我在调试时发现网页一切正常，但安卓上总是无法正常加载，找了好半天BUG、、

### 安卓实现

找到了一个开源项目[MapCompose](https://github.com/p-lr/MapCompose)完美地契合了我的需求。

不过显示上有一个问题，地图组件显示出的文字过小，但是由于地图的绘制方式是位图而不是矢量，并没有办法直接修改文字大小，于是想到把地图组件强制放大。最后通过在地图组件上添加了一个`Modifier.fillMaxSize(0.5f).scale(2f)`即可放大两倍（相当于先在一半的大小上绘制地图，再拉到全屏大小），放大到其他倍数也同理。

另外，为了加快加载速度，并且让地图能够离线查看，在`OkHttp`上添加了一个`cacheControl`实现缓存。

## 网页端BIT101模块

自动登录`BIT101`功能的实现已经在之前介绍过了。

### 通过监测路由实现的功能

当识别到路由切换到成绩查询页面时，会执行一段`JavaScript`代码，以实现自动填入学号密码的功能。

而当识别到路由切换到非`BIT101`站点时，就会拦截在`WebView`中的访问，并跳转到系统浏览器访问，以避免复杂情况的处理和安全问题。


### WebView状态问题

每次在应用内切换导航又回到`WebView`所在页面时，就会发现`WebView`的状态已经改变了。这是由于只使用了`rememberWebViewState`，查了半天翻到了`GitHub`上的[一个issue](https://github.com/google/accompanist/pull/1557)，这个问题在一两个星期前通过一个新的`rememberSaveableWebViewState`解决了，将库更新到最新的`alpha`版本并更改为使用`rememberSaveableWebViewState`即可保存简单状态。

然而，现在的实现并没有完全恢复状态，只是单纯回到之前的页面和浏览位置（比如输入框的内容会丢失），但也先凑合用着吧。

### WebView文件上传实现

`WebView`默认是不支持文件上传的，必须通过手动重写`WebChromeClient`的`onShowFileChooser`函数来实现。我们首先在`Application`中定义一个`ActivityResultRegistry`，然后在`MainActivity`中将此设置为`ActivityResultContracts.GetMultipleContents()`，最后通过一个全局的`MutableStateFlow`传递文件选择的结果。

这部分逻辑比较绕，主要参考了[这篇文章](https://blog.csdn.net/LiePy/article/details/125797893)。


## 全局功能

### 底部导航栏隐藏动画

在某些界面中我们不希望底部导航栏显示，在`Material3`中，底部导航栏是在顶层组合函数的`Scaffold`中定义的，所以配置也只能在顶层中完成。实现方法也并不复杂，只需要建立一个以路由为线索的`State`，然后再将导航栏使用`AnimatedVisibility`包裹即可。`Material3`中的动画实现还是很容易的，但是这里又遇到了一个问题，在导航栏切换的过程中已经切换到另外一个界面了，切换前后`Padding`会改变，这就导致了界面布局会有一个奇怪的抖动，而这个`Padding`实际上是由`NavHost`负责传下去，由下面的节点自行处理的，所以那些不需要导航栏的界面只要完全不理会传入的`Padding`就可以了。

### 关于主题

`BIT101`的主视觉色分别为色相为`24`的橙色（如<b style="color:#FF9A57">#FF9A57</b>）和色相为`192`的蓝色（如<b style="color:#00ABD6">#00ABD6</b>）。

首先通过官方的[Material Theme Builder](https://m3.material.io/theme-builder)网站简单生成了一套配色文件（`Color.kt`、`Theme.kt`），然后自行调整了`Color.kt`的配色方案。`Android12+`提供了根据系统设置动态生成配色的`API`，所以希望能够让用户自行决定是否打开这项功能，另外还希望用户能够决定是否启用深色模式，这些配置在设置界面被设置后将被写入`DataStore`，然后在`Theme.kt`文件中编辑`BIT101Theme`中进行监听，一旦状态改变就变更主题。由于`BIT101Theme`是整个应用的根结点，所以当它引起重构时，整个应用的主题也就改变了。

为了达到沉浸式的体验，还想要更改顶部的系统状态栏和底部的系统导航栏颜色与应用内颜色相适配。这只需要在`Activity`中更改`window.statusBarColor`和`window.navigationBarColor`即可实现。另外，当顶部状态栏为浅色或深色时，还需要同步更改顶部状态栏文字的颜色。这些颜色必须跟着主题的改变而改变，于是就将这部分代码放在`BIT101Theme{}`函数内，这样每次主题改变，在重构过程中就会被调用，而在其他情况下则不会被调用造成性能问题。


## 一些其他问题

### 点击时的波纹效果与组件形状

在很多情况下波纹的形状都会自动与组件形状相适配，但是一些情况（比如使用`Modifier.clickable{}`定义点击响应）下，波纹的形状会变成一个矩形，这时可以使用`Modifier.clip()`将组件的形状传递给波纹。

### rememberSaveable导致的闪退问题

在使用`rememberSaveable`保存自定义对象时，一旦切换页面就会闪退，这是由于触发保存操作时，默认的序列化过程无法处理自定义对象。解决方法是要么手动定义对象的保存和恢复操作，或者直接使用`remember`代替`rememberSaveable`。

### 引入Java库问题

乐学的日程是`.ics`格式的，我使用了`iCal4j`库来解析，然而这个库并没有`kotlin`版本，于是就只能直接引入`Java`版本的，然而在编译时却会出现一些依赖库出现冲突，我尝试了很多方法都无法解决，报错信息也并不明确，幸运的是我最终在一个[GitHub仓库中](https://github.com/bitfireAT/ical4android/blob/main/build.gradle)找到了解决方法，只需要在`build.gradle`中添加一些`exclude`语句排除一些有冲突的依赖库即可。


### 自动建立并显示开源声明

在本项目中使用了大量的第三方库，它们使用使用了各种不同的开源协议，出于对开源精神的尊重，应该在应用内添加一个显示所使用的开源库的功能。

然而，如果手动确认使用的开源库及其开源协议，这项工作将会非常繁琐，所以我使用了一个开源的`gradle`插件[gradle-license-plugin](https://github.com/jaredsburrows/gradle-license-plugin)来对项目中使用的开源库进行自动扫描，并生成一个`txt`文件放在`assets`目录下，然后在应用中读取这个文件并显示出来即可。

使用时，只需要运行`gradle licenseDebugReport`任务即可生成一个存放在`assets`下的`txt`文件，然后在应用中读取并显示即可。

### 打包时混淆压缩问题

打包时如果在`build.gradle`中设置`minifyEnabled true`，则打包时会对代码进行压缩混淆，原来`15MB`左右的安装包能压缩到惊人的`4MB+`，然而，安装后却发现部分功能无法正常使用，原因是一些依赖包运用到反射等特性，是不支持混淆压缩的。如果仍想要进行混淆压缩，需要手动配置排除一些依赖包，由于时间有限，且不压缩的`APK`本身也就不到`20MB`，也就先这样了，之后可以再优化。

### Compose BoM的使用

`Jetpack Compoe`更新迭代很快，各种包又很多，而且互相之间还有版本要求依赖关系。经常因为一些功能需要更改包版本，更改后又编译不通过了。

后面发现了`Compose BoM`，真乃神器！只需要一行代码就完成了所有`Compose`包的版本管理。

### Compose性能优化原则

结合官方文档、一些博客和自己的实践，总结了一些能优化`Compose`效率的原则：
* 状态不改变的代码不要在`Compose`函数中执行，全部放到`ViewModel`、`remember{}`或`LaunchedEffect`里，避免重构时反复运行。
* 状态提升原则：
* 

（我超 突然发现交上去的文档这里没写完呜呜呜）

## 项目亮点

* 选题贴合实际，有很大的实用价值
* 使用`Kotlin`+`Jetpack Compose`编写，紧跟时代
* `UI`使用`Material Design 3`组件构建，应用`Material You`设计思路
* 数据和状态大量运用`Flow`等响应式类型，传输路径清晰
* 所有`IO`密集型操作均在协程中完成，执行效率高
* 充分考虑安全问题，保障用户隐私
* 使用`GitHub`进行开源和代码管理
* 撰写了详细的文档，其他同学也可以贡献开发，项目具有可持续性

## 未来开发计划

* 通过小组件在桌面线显示课程日程
* 使用生物识别提升安全性能
* 使用`NFC`功能实现刷校园卡登录（可行性未验证）
* 使用加密方式管理`Cookie`
* 其他`BIT101`平台功能升级
