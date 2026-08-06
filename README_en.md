# BIT101-Android

<!-- hy-mt2-i18n:start -->
[中文](./README.md) | **English** | [日本語](./README_ja.md) | [Español](./README_es.md)
<!-- hy-mt2-i18n:end -->


* Website homepage: [android.bit101.cn](http://android.bit101.cn/)
* Main project link: [BIT101](https://github.com/flwfdd/BIT101)
* Server link: [BIT101-GO](https://github.com/flwfdd/BIT101-GO)
* `API` documentation: [API](https://j1dds2ogfu.apifox.cn/)

## System Requirements

To install and run `BIT101-Android`, Android 8.0 or higher is required, while dynamic system theme adaptation requires Android 12 or higher.

## Supported Features

* Automatically sync and display the class schedule  
* Automatically pull and display schedules from Lexue  
* Campus map  
* Include all features of the `BIT101` website  
* Customize display effect settings

## Planned Features

* Manually add schedules  
* Manually modify the class schedule  
* Display classes and schedules in desktop group views  
* Map display with location tracking and navigation features

Feel free to share your ideas✨

# Strict Constraints
1. **Structure Locking**: Maintain the original Markdown data structure, indentation, heading levels, tables, links, URLs, badges, code blocks, and inline codes exactly as they are.
2. **Selective Translation**: Only translate visible natural language content intended for users.
3. **Prohibition of Modifications**: It is strictly forbidden to translate or alter code tags, key names, variable placeholders (such as {{var}}, ${var}, %s, %d, etc.), command examples, file paths, project names, API names, package names, model names, identifiers, and code symbols; unless a corresponding translation is already provided in the background information.
4. The translations of terms, styles, and proper nouns should be consistent with those in the given background information.

This project was also used as the final assignment for the `Android` course in the second semester of sophomore year; the following is excerpted from the submitted document.

## Code Architecture and Implementation

Note: Due to space constraints, only the parts directly related to the client are covered on the server side. For more detailed explanations of the server side, please refer to [GitHub](https://github.com/flwfdd/BIT101-GO).

![Architecture](https://github.com/user-attachments/assets/a378649a-85dd-46ce-b779-0f2ec4ae5f95)

The project adopts an `MVVM` architecture overall, featuring three layers of data models: the underlying `Model` layer is responsible for processing and providing data, the middle `ViewModel` layer handles data integration and manages runtime states, while the top `View` layer interacts with users using the data and states provided by the model layer.

Specifically, the lower-level `Model` layer primarily utilizes the following data sources:  
* `DataStore`: Responsible for storing simple variables and settings.  
* `Room`: Provides access to `SQLite` databases, storing courses, schedules, and other data.  
* `OkHttp`: Handles network interactions with school APIs, and also uses `Cookie Store` to save `Cookies` during these interactions.  
* `Retrofit`: Facilitates network communication with the `BIT101` server.  
* `Jetpack Security`: Offers system-level secure storage methods for saving passwords.


The middle `ViewModel` layer separates the data layer from the UI layer, employing full reactive design by utilizing `Flow` and `State` for data transmission wherever possible. Additionally, all data retrieval operations are executed within coroutines to avoid performance issues.

Several open-source libraries are also used for data processing:  
* `Gson`: Used to handle strings in `JSON` format.  
* `iCal4j`: Used to parse strings in the `iCalendar` calendar standard format obtained from Lexue.


The top-level `View` layer adopts a single `Activity` architecture, implemented using `Jetpack Compose` + `Material Design 3`, and manages data and state by binding to the `ViewModel`. Additionally, several other open-source components are utilized:
* `Navigation Compose`: for page switching and navigation management
* `MapCompose`: for displaying maps
* `Accompanist Webview`: for showing the `BIT101` web interface
* `Coil`: for displaying images


Next, we will introduce the implementation of certain features module by module.

## Login Module

### Automatic Management of School Interfaces and Cookies

The interfaces of the school website are primarily implemented through `Cookies` and various redirects, requiring third-party authentication from `login.bit.edu` for all other pages. Simulating this process entirely manually would be extremely cumbersome and difficult to achieve. In the absence of `RESTful APIs`, using a high-level framework like `Retrofit` becomes inconvenient; the best approach is to fully mimic browser behavior. Therefore, my solution involves using `OkHttp` along with a [cookie-store](https://github.com/gotev/android-cookie-store) middleware to handle automatic `Cookie` management, allowing users to access the interfaces just like in a browser, with authentication automatically handled via `Cookies` and redirects.

#### Unified Authentication Process

The school's unified authentication is handled by `BIT-Login v4.0.0`, which includes service callbacks as well as campus network or `WebVPN` environment detection. After a successful login, the app synchronizes the complete `Cookie` properties from the SDK session to the persistent `CookieStore`, then shuts down the SDK’s Ktor client.

Business requests such as courses, exams, semesters, available classrooms, and the Lexue calendar are still handled by the existing `Retrofit` services, maintaining the school session through the same persistent `CookieStore`. The login process for the `BIT101` backend and the `fake-cookie` protocol remain unchanged, with password encryption still performed by `AESUtils`.

The current login interface does not yet support interactions with graphic verification codes or SMS verification codes. When the SDK encounters such processes, it will report them upward through the existing error pathways rather than silently treating the login as successful.

The current build toolchain includes Kotlin 2.4.10, Compose Compiler Kotlin plugin 2.4.10, KSP 2.3.10, AGP 8.13.2, Gradle 8.13, Hilt 2.57.2, and Room 2.8.4. BIT-Login’s Android dependencies require `compileSdk 36`, while the app still maintains `targetSdk 34`.

### Password Management

Since an automatic re-login feature needs to be implemented, it is necessary to save the student ID and password. However, storing such sensitive information directly is clearly inappropriate. Therefore, I used `Security` from `Jetpack` to manage passwords securely. This library utilizes a hardware-encrypted `KeyStore` to generate and store keys, which are then used together with `EncryptedSharedPreferences` to store data, thereby ensuring the security of user account passwords.

However, the downside is that `cookie-store` uses unencrypted `SharedPreferences` to store `Cookies`, which also poses security risks. Due to time constraints, the storage interface for this part has not been rewritten yet.

### BIT101 Login

The APIs for the `BIT101` section are implemented using `Retrofit`. During login, the student ID and unified identity authentication password are first used to interact with the `BIT101` server to obtain a `token` in `JWT` format along with a `code` composed of numbers (the password transmitted in this process cannot be decrypted by the server and is only used to forward to the school server for authentication). Subsequently, the obtained `token` and `code` are used to send a login request to the `BIT101` server, which will then issue a `fake-cookie` for login verification. If no `BIT101` account has been registered, one will be created with the unified identity authentication password set as the default password.

After obtaining the `fake-cookie`, it is necessary to maintain the login status in subsequent requests. Since the permission verification for `BIT101` interfaces does not require `Cookies` but instead uses a custom `fake-cookie` header in requests, another `OkHttp` `Client` is created and equipped with a middleware that automatically adds the `fake-cookie` to the `Header`. This `Client` can then be assigned to `Retrofit`.

The `fake-cookie` for the `BIT101` web interface is stored in `localStorage`. Therefore, after obtaining the `fake-cookie` through `Retrofit` login, it is sufficient to use `JavaScript` code to inject the `fake-cookie` into `localStorage` when the `WebView` is launched.

## Course and Schedule Module

The class schedule can be accessed via redirection authorization through `login.bit.edu.cn` by interacting with the school’s educational administration system and other backends. The schedule is obtained by using the “Export Calendar” function in Lexue to get a subscription link, after which the schedule details are retrieved through that link. These interfaces are described in detail in the [API documentation](https://j1dds2ogfu.apifox.cn/), so no further explanation is needed here. The course and schedule data obtained from the school’s APIs are stored in the database using `Room`, and then retrieved and displayed based on the configuration items in `DataStore` (such as semester and day of the week).

### Implementing with Swipeable Tabs

This part is implemented using the [Accompanist Pager](https://google.github.io/accompanist/pager/) library, which has now been integrated into `Jetpack Compose`.

The `Indicator` blocks for swiping left and right at the top are modified based on the example in the [official documentation](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#TabRow(kotlin.Int,androidx.compose.ui.Modifier,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,kotlin.Function1,kotlin.Function0,kotlin.Function0)), with some scaling effects added to make them appear jelly-like when moved, resulting in excellent animation effects. It would be quite difficult to create such animations from scratch.

Since `Pager` internally uses `LazyRow`, there is a shadow effect when switching to the edge, which looks unattractive. After extensive searching, a solution was found—wrap the component with `CompositionLocalProvider(LocalOverscrollConfiguration provides null){}`. To enable the shadow effect, simply define an `OverscrollConfiguration` and pass it in.



## Map Module

### Data Source

The data for the map module comes from [OpenStreetMap](https://www.openstreetmap.org/), an open-source mapping project. Members of the Network Pioneers Association at Beijing Institute of Technology once created detailed campus maps on it and even developed an online map website [A BIT of Map](https://map.bitnp.net/). We are very grateful for their contributions.

However, `OpenStreetMap` suffers from DNS pollution in China, making it often impossible to access from clients. So I first set up a map tile service using [openstreetmap-tile-server](https://github.com/Overv/openstreetmap-tile-server) based on `Docker` on the server and deployed maps for Beijing. But after deployment, I found it consumed excessive server resources and was vulnerable to attacks, so I eventually gave up this approach and opted to use server proxy forwarding to solve the problem.

The final solution was to first modify the `hosts` file on the server to prevent DNS pollution, and then create a proxy rule in `Nginx`. When accessing `https://map.bit101.flwfdd.xyz/tile/{z}/{x}/{y}.png`, the request would be forwarded to `https://tile.openstreetmap.org/{z}/{x}/{y}.png`. Although the access speed is relatively slow, since the map data hardly changes, caching can be enabled in `Nginx`, allowing the data for the school area to be served directly from the server.

Also note that a `User-Agent` must be provided when calling the `OpenStreetMap` API; otherwise, the request will be blocked. During debugging, I found that everything worked fine on the web version, but it couldn’t load properly on Android, and I spent a long time trying to find the bug.

### Android Implementation

I found an open-source project [MapCompose](https://github.com/p-lr/MapCompose) that perfectly met my needs.

However, there is a display issue: the text shown in the map component is too small. Since the map is rendered as a bitmap rather than a vector, it’s not possible to directly adjust the text size. So I thought of forcing the map component to scale up. Eventually, adding `Modifier.fillMaxSize(0.5f).scale(2f)` to the map component achieved a two-fold enlargement (essentially drawing the map at half size first and then expanding it to full screen size), and the same method can be used for other scaling factors as well.

Additionally, to speed up loading and enable offline map viewing, a `cacheControl` was added to `OkHttp` to implement caching.

## BIT101 Module on the Web Page

The implementation of the automatic login to `BIT101` feature was already explained earlier.

### Features Achieved through Route Monitoring

When it detects that the route has switched to the grade inquiry page, it executes some `JavaScript` code to automatically fill in the student ID and password.

When it detects that the route has switched to a non-BIT101 site, it intercepts the request within the WebView and redirects it to the system browser to avoid dealing with complex scenarios and security issues.


### WebView State Issues

Every time navigation is switched within the app and the page containing the `WebView` is returned to, it is found that the state of the `WebView` has changed. This is because only `rememberWebViewState` was used. After searching for a long time, I found [an issue](https://github.com/google/accompanist/pull/1557) on GitHub. This problem was resolved a week or two ago with a new `rememberSaveableWebViewState`. Updating the library to the latest `alpha` version and switching to using `rememberSaveableWebViewState` allows simple states to be saved.

However, the current implementation does not fully restore the state; it only returns to the previous page and browsing position (for example, the content in input fields is lost), but it can be used as a temporary solution for now.

### Implementation of file upload in WebView

By default, `WebView` does not support file uploads; this functionality must be implemented by manually overriding the `onShowFileChooser` method of `WebChromeClient`. We first define an `ActivityResultRegistry` in the `Application`, then set it to `ActivityResultContracts.GetMultipleContents()` in `MainActivity`, and finally pass the file selection results through a global `MutableStateFlow`.

This logic is quite complex, and I mainly referred to [this article](https://blog.csdn.net/LiePy/article/details/125797893).


## Global Features

### Bottom Navigation Bar Hidden Animation

In some interfaces, we don’t want the bottom navigation bar to be displayed. In `Material3`, the bottom navigation bar is defined within the `Scaffold` of the top-level composable, so configurations can only be done at that level as well. The implementation isn’t complicated—just create a `State` tied to the route, then wrap the navigation bar with `AnimatedVisibility`. Animations in `Material3` are relatively easy to handle, but another issue arises here: if another interface has already been loaded during the navigation bar transition, the `Padding` changes, causing strange layout jitter. Since this `Padding` is actually passed down by `NavHost` for the underlying components to manage, interfaces that don’t require a navigation bar can simply ignore the incoming `Padding`.

### About the Theme

The primary colors of `BIT101` are orange with a hue of `24` (such as <b style="color:#FF9A57">#FF9A57</b>) and blue with a hue of `192` (such as <b style="color:#00ABD6">#00ABD6</b>).

First, a set of color configuration files (`Color.kt`, `Theme.kt`) were generated using the official [Material Theme Builder](https://m3.material.io/theme-builder) website, after which the color scheme in `Color.kt` was adjusted manually. `Android12+` provides `API`s to dynamically generate colors based on system settings, so it is desired to allow users to decide whether to enable this feature, as well as whether to turn on dark mode. Once these settings are configured in the settings interface, they will be written to `DataStore`, and `BIT101Theme` in `Theme.kt` will be monitored to change the theme whenever the status changes. Since `BIT101Theme` is the root node of the entire app, changing it will alter the theme of the whole application.

To achieve an immersive experience, we also want to adjust the colors of the top system status bar and bottom system navigation bar to match the app’s internal colors. This can be done simply by modifying `window.statusBarColor` and `window.navigationBarColor` in the `Activity`. Additionally, when the top status bar is light or dark, the color of the text in it needs to be changed accordingly. Since these colors must change along with the theme, this code is placed inside the `BIT101Theme{}` function so that it is called whenever the theme changes during a rebuild, rather than being invoked unnecessarily in other situations that could cause performance issues.


## Some Other Issues

### Ripple Effect on Click and Component Shape

In many cases, the ripple shape automatically adapts to the component's shape. However, in certain scenarios—such as when using `Modifier.clickable{}` to define click behavior—the ripple becomes rectangular. In such situations, `Modifier.clip()` can be used to pass the component's shape to the ripple.

### Crash issue caused by rememberSaveable

When using `rememberSaveable` to save custom objects, the app crashes as soon as the page is switched due to the default serialization process being unable to handle custom objects at the time of saving. The solution is either to manually define the object's saving and restoring operations or to use `remember` instead of `rememberSaveable`.

### Issues with Introducing Java Libraries

The schedule in LeXue is in `.ics` format, and I used the `iCal4j` library to parse it. However, there is no Kotlin version of this library, so I had to directly use the Java version. But during compilation, conflicts arose with some dependency libraries, and despite trying many solutions with unclear error messages, I was fortunate enough to find a solution in a [GitHub repository](https://github.com/bitfireAT/ical4android/blob/main/build.gradle). All that was needed was to add some `exclude` statements in `build.gradle` to remove the conflicting dependency libraries.


### Automatically generate and display the open-source statement

This project uses a large number of third-party libraries, each governed by different open-source licenses. Out of respect for the open-source philosophy, it is necessary to add a feature within the app that displays the open-source libraries being used.

However, manually identifying the open-source libraries used and their respective licenses would be a very tedious task. Therefore, I used an open-source `gradle` plugin [gradle-license-plugin](https://github.com/jaredsburrows/gradle-license-plugin) to automatically scan the libraries in the project, generate a `txt` file that is placed in the `assets` directory, and then read this file in the app to display it.

To use it, simply run the `gradle licenseDebugReport` task to generate a `txt` file in the `assets` directory, which can then be read and displayed within the app.

### Minification and Obfuscation Issues During Packaging

During packaging, if `minifyEnabled true` is set in `build.gradle`, the code will be compressed and obfuscated, reducing an originally ~15MB installation package to an astonishing ~4MB+. However, after installation, some features may fail to work properly because certain dependency packages use features like reflection that are not compatible with obfuscation. If obfuscation is still desired, manual configuration to exclude specific dependency packages is required. Due to time constraints—and since the uncompressed APK is already less than 20MB—this was left as is for now, with further optimization possible in the future.

### Using Compose BoM

Jetpack Compose evolves rapidly, with numerous packages that have their own version dependency requirements among them. It's common to need to change package versions for certain features, only to encounter compilation failures after making those changes.

Later, I discovered `Compose BoM`—it’s truly a game-changer! With just one line of code, it handles version management for all `Compose` dependencies.

### Principles for Optimizing Compose Performance

Based on official documentation, various blogs, and my own experiences, I have summarized some principles to optimize `Compose` performance:  
* Code whose state does not change should not be executed within `Compose` functions; instead, move it all to `ViewModel`, `remember{}`, or `LaunchedEffect` to avoid repeated execution during refactoring.  
* State elevation principle:  
*

(I just suddenly realized that the document I submitted isn’t finished here, waaah!)

## Project Highlights

* The selected topic is practical and holds great utility value.  
* Developed using `Kotlin` + `Jetpack Compose`, keeping up with current trends.  
* The `UI` is built with `Material Design 3` components, applying the design philosophy of `Material You`.  
* Data and state make extensive use of reactive types such as `Flow`, ensuring clear data flow paths.  
* All `IO`-intensive operations are performed within coroutines, resulting in high execution efficiency.  
* Security concerns are thoroughly addressed to protect user privacy.  
* Open source development and code management are handled via `GitHub`.  
* Detailed documentation is provided, allowing other students to contribute to development and ensuring the project’s sustainability.

## Future Development Plans

* Display the course schedule on the desktop interface via widgets  
* Enhance security performance using biometric recognition  
* Implement campus card login via NFC functionality (feasibility not verified)  
* Manage Cookies using encryption methods  
* Other upgrades to the BIT101 platform features
