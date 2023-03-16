# BIT101-Android

项目仍在开发中，敬请期待。

## Tab实现

首先是`Indicator`的实现，基于[官方文档](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#TabRow(kotlin.Int,androidx.compose.ui.Modifier,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,kotlin.Function1,kotlin.Function0,kotlin.Function0))的例子进行修改，实现了非常好的动画效果，这样的动画完全自己写的话还是比较困难。

然后是想要实现左右滑动切换的功能，找到了一个[Accompanist Pager](https://google.github.io/accompanist/pager/)库，文档中说官方库已经支持了这个功能，但是需要`Compose 1.4.0+`，然而我升级了`Compose`后发现还有很多库（比如`androidx.compose.ui`）还没有支持到`1.4.0+`，就只好退回来用原来的库了。
