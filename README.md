# BIT101-Android

项目仍在开发中，敬请期待。

---
由于本项目同时作为大二下安卓课的课程设计，最后会需要写文档，所以会在此记录一些开发过程中遇到的问题等。

## Tab实现

首先是`Indicator`的实现，基于[官方文档](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#TabRow(kotlin.Int,androidx.compose.ui.Modifier,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,kotlin.Function1,kotlin.Function0,kotlin.Function0))的例子进行修改，实现了非常好的动画效果，这样的动画完全自己写的话还是比较困难。

然后是想要实现左右滑动切换的功能，找到了一个[Accompanist Pager](https://google.github.io/accompanist/pager/)库，文档中说官方库已经支持了这个功能，但是需要`Compose 1.4.0+`，然而我升级了`Compose`后发现还有很多库（比如`androidx.compose.ui`）还没有支持到`1.4.0+`，就只好退回来用原来的库了。

因为其内部是使用`LazyRow`实现的，所以混动切换到边界时会有阴影效果，不太美观。经过百般搜寻，才找到了去掉的办法——使用一个`CompositionLocalProvider(LocalOverscrollConfiguration provides null){}`包裹，想要设置阴影效果的话定义一个`OverscrollConfiguration`并传入即可。

## 底部导航栏隐藏动画

在某些界面中我们不希望底部导航栏显示，在Material3中，底部导航栏是在顶层组合函数的`Scaffold`中定义的，所以配置也只能在顶层中完成。实现方法也并不复杂，只需要建立一个以路由为线索的`State`，然后再将导航栏使用`AnimatedVisibility`包裹即可。Material3中的动画实现还是很容易的，但是这里又遇到了一个问题，在导航栏切换的过程中已经切换到另外一个界面了，切换前后`Padding`会改变，这就导致了界面布局会有一个奇怪的抖动，而这个`Padding`实际上是由`NavHost`负责传下去，由下面的节点自行处理的，所以那些不需要导航栏的界面只要完全不理会传入的`Padding`就可以了。
