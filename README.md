# BIT101-Android

项目仍在开发中，敬请期待。

---
由于本项目同时作为大二下安卓课的课程设计，最后会需要写文档，所以会在此记录一些开发过程中遇到的问题等。

## 登陆模块

### 学校接口和Cookie自动管理

学校网站的接口基本都是通过`Cookie`和各种各样的重定向来实现的，所有的其他页面都需要`login.bit.edu`的第三方认证，这个过程如果完全通过手动模拟完成将会非常痛苦，很难实现。在没有`RESTful API`的情况下，使用`Retrofit`这样高层次的框架反而不方便了，最好的方式就是完全模拟浏览器的行为。所以我的实现方式是使用`OkHttp`，并且通过加入一个[cookie-store](https://github.com/gotev/android-cookie-store)中间件实现`Cookie`的全自动管理，这样只需要像使用浏览器一样访问接口就可以了，权限验证将自动通过`Cookie`和重定向完成。

### 登陆流程

### 密码管理

由于需要实现自动重新登陆的功能，因此必须要保存学号和密码，但是直接保存这样的敏感信息显然不合适，因此我使用了`Jetpack`中的`Security`实现了密码的安全管理，这个库会使用的硬件加密的`KeyStore`生成和存储密钥，再用密钥配合`EncryptedSharedPreferences`存储数据，这样就可以保证用户帐号密码的安全。

另外美中不足的是，`cookie-store`是使用未加密的`SharedPreferences`存储`Cookie`的，这也会带来安全隐患，但由于时间限制，我暂时还没有重写这部分的存储接口。

## Tab实现

首先是`Indicator`的实现，基于[官方文档](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#TabRow(kotlin.Int,androidx.compose.ui.Modifier,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,kotlin.Function1,kotlin.Function0,kotlin.Function0))的例子进行修改，实现了非常好的动画效果，这样的动画完全自己写的话还是比较困难。

然后是想要实现左右滑动切换的功能，找到了一个[Accompanist Pager](https://google.github.io/accompanist/pager/)库，文档中说官方库已经支持了这个功能，但是需要`Compose 1.4.0+`，然而我升级了`Compose`后发现还有很多库（比如`androidx.compose.ui`）还没有支持到`1.4.0+`，就只好退回来用原来的库了。

因为其内部是使用`LazyRow`实现的，所以混动切换到边界时会有阴影效果，不太美观。经过百般搜寻，才找到了去掉的办法——使用一个`CompositionLocalProvider(LocalOverscrollConfiguration provides null){}`包裹，想要设置阴影效果的话定义一个`OverscrollConfiguration`并传入即可。

## 底部导航栏隐藏动画

在某些界面中我们不希望底部导航栏显示，在`Material3`中，底部导航栏是在顶层组合函数的`Scaffold`中定义的，所以配置也只能在顶层中完成。实现方法也并不复杂，只需要建立一个以路由为线索的`State`，然后再将导航栏使用`AnimatedVisibility`包裹即可。`Material3`中的动画实现还是很容易的，但是这里又遇到了一个问题，在导航栏切换的过程中已经切换到另外一个界面了，切换前后`Padding`会改变，这就导致了界面布局会有一个奇怪的抖动，而这个`Padding`实际上是由`NavHost`负责传下去，由下面的节点自行处理的，所以那些不需要导航栏的界面只要完全不理会传入的`Padding`就可以了。

## 地图模块

### 数据来源

地图模块的数据使用了[OpenStreetMap](https://www.openstreetmap.org/)，这是一个开源的地图项目，北京理工大学网络开拓者协会的成员曾在上面绘制了详细的校园地图，还建立了一个在线地图网站[一点儿北理地图(A BIT of Map)](https://map.bitnp.net/)，非常感谢他们的贡献。

然而，`OpenStreetMap`在国内存在DNS污染的问题，客户端上往往无法正常访问，于是我先是在服务器上使用基于`Docker`的[openstreetmap-tile-server](https://github.com/Overv/openstreetmap-tile-server)搭建了地图瓦片服务，并部署了北京地区的地图，但是部署后发现占用服务器资源过大，很容易受到攻击，所以后来还是放弃了这个方案，使用服务器代理转发的方式来解决问题。

最终的解决方案是，先在服务器上修改`hosts`防止DNS污染，然后在`Nginx`上建立一个代理规则。当访问`https://map.bit101.flwfdd.xyz/tile/{z}/{x}/{y}.png`时，请求将被转发到`https://tile.openstreetmap.org/{z}/{x}/{y}.png`，虽然访问速度比较慢，但由于地图数据基本不会改变，所以可以在`Nginx`上加入缓存，学校地区的数据基本上就是用服务器本地的了。

另外注意，调用`OpenStreetMap`接口时需要传入`User-Agent`，否则会被拦截。我在调试时发现网页一切正常，但安卓上总是无法正常加载，找了好半天BUG、、

### 安卓实现

找到了一个开源项目[MapCompose](https://github.com/p-lr/MapCompose)完美地契合了我的需求。

不过显示上有一个问题，地图组件显示出的文字过小，但是由于地图的绘制方式是位图而不是矢量，并没有办法直接修改文字大小，于是想到把地图组件强制放大。最后通过在地图组件上添加了一个`Modifier.fillMaxSize(0.5f).scale(2f)`即可放大两倍（相当于先在一半的大小上绘制地图，再拉到全屏大小），放大到其他倍数也同理。

另外，为了加快加载速度，并且让地图能够离线查看，在`OkHttp`上添加了一个`cacheControl`实现缓存。

## 一些UI显示上的问题

### 点击时的波纹效果与组件形状

在很多情况下波纹的形状都会自动与组件形状相适配，但是一些情况（比如使用`Modifier.clickable{}`定义点击响应）下，波纹的形状会变成一个矩形，这时可以使用`Modifier.clip()`将组件的形状传递给波纹。

### `rememberSaveable`导致的闪退问题

在使用`rememberSaveable`保存自定义对象时，一旦切换页面就会闪退，这是由于触发保存操作时，默认的序列化过程无法处理自定义对象。解决方法是要么手动定义对象的保存和恢复操作，或者直接使用`remember`代替`rememberSaveable`。

### 引入`Java`库问题

乐学的日程是`.ics`格式的，我使用了`ical4j`库来解析，然而这个库并没有`kotlin`版本，于是就只能直接引入`Java`版本的，然而在编译时却会出现一些依赖库出现冲突，我尝试了很多方法都无法解决，报错信息也并不明确，幸运的是我最终在一个[GitHub仓库中](https://github.com/bitfireAT/ical4android/blob/main/build.gradle)找到了解决方法，只需要在`build.gradle`中添加一行`exclude`语句排除一些有冲突的依赖库即可。
