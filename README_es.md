# BIT101-Android

<!-- hy-mt2-i18n:start -->
[中文](./README.md) | [English](./README_en.md) | [日本語](./README_ja.md) | **Español**
<!-- hy-mt2-i18n:end -->


* Página principal del sitio web: [android.bit101.cn](http://android.bit101.cn/)
* Enlace al proyecto principal: [BIT101](https://github.com/flwfdd/BIT101)
* Enlace al servidor: [BIT101-GO](https://github.com/flwfdd/BIT101-GO)
* Documentación de la `API`: [API](https://j1dds2ogfu.apifox.cn/)

## Requisitos del sistema

Para instalar y ejecutar `BIT101-Android` se requiere Android 8.0 o superior, mientras que la función de adaptación dinámica al tema del sistema necesita Android 12 o superior.

## Funciones soportadas

* Sincronización y visualización automáticas del horario de clases  
* Obtención y muestra automática de los compromisos desde Lexue  
* Mapa del campus  
* Todas las funciones del sitio web `BIT101`  
* Configuración personalizada de la presentación

## Funciones previstas

* Añadir eventos manualmente  
* Modificar el horario de clases manualmente  
* Mostrar clases y eventos en grupos del escritorio  
* Funciones de localización y navegación en el mapa

¡Bienvenido a compartir tus ideas✨

---

Este proyecto también se utilizó como tarea final del curso de `Android` en la segunda mitad del segundo semestre, y lo siguiente está extraído de los documentos presentados.

## Arquitectura y implementación del código

Nota: Debido a limitaciones de espacio, el servidor solo presenta algunas partes relacionadas directamente con la aplicación cliente; para obtener una descripción más detallada del servidor, consulte [GitHub](https://github.com/flwfdd/BIT101-GO).

![Arquitectura](https://github.com/user-attachments/assets/a378649a-85dd-46ce-b779-0f2ec4ae5f95)

El proyecto en su conjunto sigue la arquitectura `MVVM` y cuenta con tres capas de modelo de datos: la capa `Model` inferior se encarga de procesar y proporcionar los datos, la capa `ViewModel` intermedia se ocupa de conectar con los datos y gestionar el estado de ejecución, mientras que la capa `View` superior utiliza los datos y estados proporcionados por la capa de modelo para interactuar con el usuario.

En concreto, la capa `Model` inferior utiliza principalmente las siguientes fuentes de datos:  
* `DataStore`: se encarga de almacenar variables simples y configuraciones.  
* `Room`: ofrece acceso a bases de datos `SQLite`, donde se almacenan cursos, horarios, etc.  
* `OkHttp`: proporciona interacción en red con las interfaces de la institución, y también emplea `Cookie Store` para guardar los `Cookie` durante las conexiones.  
* `Retrofit`: permite la interacción en red con el servidor de `BIT101`.  
* `Jetpack Security`: método de almacenamiento seguro a nivel del sistema, utilizado para guardar contraseñas.


La capa intermedia `ViewModel` separa la capa de datos de la capa visual, empleando un diseño reactivo en su totalidad; para la transmisión de datos se utiliza preferentemente `Flow` y `State`, al tiempo que todas las operaciones de obtención de datos se realizan dentro de corutinas a fin de evitar problemas de rendimiento.

También se utilizan varias bibliotecas de código abierto para el procesamiento de datos:  
* `Gson`: para manejar cadenas de texto en formato `JSON`.  
* `iCal4j`: para analizar cadenas en formato estándar de calendario `iCalendar` obtenidas desde Lexue.


La capa superior `View` sigue una arquitectura de un solo `Activity`, implementada con `Jetpack Compose` + `Material Design 3`, y gestiona los datos y el estado mediante el enlace al `ViewModel`. Además, se utilizan algunos componentes open source adicionales:  
* `Navigation Compose`: para cambiar de página y gestionar la navegación  
* `MapCompose`: para mostrar mapas  
* `Accompanist Webview`: para visualizar la versión web de `BIT101`  
* `Coil`: para mostrar imágenes


A continuación, se presentará de forma modular la implementación de algunas funcionalidades.

## Módulo de inicio de sesión

### Gestión automática de las interfaces de la escuela y las cookies

Las interfaces del sitio web de la escuela se implementan básicamente mediante `Cookie` y diversos redirecciones; todas las páginas adicionales requieren autenticación de terceros en `login.bit.edu`. Realizar este proceso exclusivamente mediante simulaciones manuales sería extremadamente complicado e inviable. En ausencia de una `RESTful API`, utilizar frameworks de nivel superior como `Retrofit` resulta poco práctico, por lo que la mejor opción es simular completamente el comportamiento del navegador. Por eso, mi enfoque consiste en usar `OkHttp` y un middleware de [cookie-store](https://github.com/gotev/android-cookie-store) para gestionar automáticamente los `Cookie`; de esta manera, basta con acceder a las interfaces como si se estuviera usando un navegador, ya que la verificación de permisos se realizará automáticamente a través de los `Cookie` y los redirecciones.

#### Proceso de autenticación unificada

La autenticación unificada de la escuela está a cargo de `BIT-Login v4.0.0`, lo que incluye las devoluciones de servicio así como el reconocimiento del entorno de red escolar o `WebVPN`. Tras un login exitoso, la aplicación sincroniza todos los atributos de `Cookie` del session del SDK con el `CookieStore` persistente y, posteriormente, cierra el cliente Ktor del SDK.

Las solicitudes relacionadas con cursos, exámenes, semestres, aulas disponibles y el calendario de Lexue siguen siendo gestionadas por los servicios existentes de `Retrofit`, manteniendo la sesión en la escuela a través del mismo `CookieStore` persistente. El proceso de inicio de sesión en el backend de `BIT101` y el protocolo de `fake-cookie` permanecen sin cambios, mientras que el cifrado de contraseñas sigue siendo realizado por `AESUtils`.

La interfaz de inicio de sesión actual aún no incluye interacción con códigos verificadores gráficos ni por mensaje de texto; cuando el SDK se encuentra con estos procesos, los reporta mediante los canales de error existentes, sin considerar silenciosamente que el inicio de sesión fue exitoso.

La cadena de herramientas de compilación actual es Kotlin 2.4.10, el complemento Compose Compiler Kotlin 2.4.10, KSP 2.3.10, AGP 8.13.2, Gradle 8.13, Hilt 2.57.2 y Room 2.8.4. Los requisitos de dependencias para Android de BIT-Login exigen `compileSdk 36`, mientras que la aplicación mantiene `targetSdk 34`.

### Gestión de contraseñas

Dado que es necesario implementar la función de autologin automático, es imprescindible guardar el número de estudiante y la contraseña. Sin embargo, almacenar directamente esta información sensible resulta claramente inadecuado, por lo que utilicé la biblioteca `Security` de `Jetpack` para gestionar de forma segura las contraseñas. Esta biblioteca genera y almacena claves mediante un `KeyStore` basado en cifrado hardware, y luego utiliza esas claves junto con `EncryptedSharedPreferences` para guardar los datos, lo que permite garantizar la seguridad de las cuentas y contraseñas de los usuarios.

Sin embargo, la desventaja es que `cookie-store` almacena las `Cookie` en un `SharedPreferences` sin cifrar, lo cual también conlleva riesgos de seguridad; pero debido a limitaciones de tiempo, por el momento aún no se ha reescrito la interfaz de almacenamiento correspondiente.

### Inicio de sesión en BIT101

Las interfaces relacionadas con `BIT101` se implementan mediante `Retrofit`. Durante el proceso de inicio de sesión, primero se interactúa con el servidor de `BIT101` utilizando el número de estudiante y la contraseña de autenticación unificada para obtener un `token` en formato `JWT` junto con un `code` numérico (la contraseña transmitida en este proceso no puede ser descifrada por el servidor y solo se utiliza para reenviarla al servidor de la escuela con fines de autenticación). A continuación, se envía una solicitud de inicio de sesión al servidor de `BIT101` con el `token` y el `code` obtenidos, y el servidor emite un `fake-cookie` para validar el inicio de sesión. Si aún no se ha registrado una cuenta en `BIT101`, se creará una cuenta automáticamente, estableciendo como contraseña predeterminada la contraseña de autenticación unificada.

Una vez obtenido el `fake-cookie`, es necesario mantener el estado de inicio de sesión en las solicitudes posteriores. Dado que la verificación de autorización de las interfaces de `BIT101` no requiere el uso de `Cookie`, sino que emplea un encabezado `fake-cookie` personalizado al enviar las solicitudes, se crea otro `Client` de `OkHttp` e se añade un middleware que inserta automáticamente el `fake-cookie` en el `Herder`; posteriormente, este `Client` se asigna a `Retrofit`.

El `fake-cookie` de la versión web de `BIT101` se almacena en `localStorage`; por lo tanto, tras obtenerlo mediante el inicio de sesión con `Retrofit`, basta con usar código `JavaScript` al iniciar la `WebView` para insertar dicho `fake-cookie` en `localStorage`.

## Módulo de cursos y horarios

El horario de clases se puede acceder mediante la autorización por redirección a `login.bit.edu.cn`, gracias a la interacción con los servidores backend del sistema administrativo académico de la institución. En cuanto al calendario, se obtiene el enlace de suscripción usando la función “Exportar calendario” de LeXue, y posteriormente se acceden los detalles del calendario a través de dicho enlace. Estas interfaces están descritas en detalle en la [documentación de API](https://j1dds2ogfu.apifox.cn/), por lo que no se explicará más aquí. Los datos de cursos y horarios obtenidos desde las interfaces de la escuela se almacenan en la base de datos mediante `Room`, y luego se recuperan y muestran según los parámetros configurados en `DataStore` (como semestre, día de la semana, etc.).

### Implementación mediante deslizamiento de pestañas hacia la izquierda y derecha

Esta funcionalidad se implementó utilizando la biblioteca [Accompanist Pager](https://google.github.io/accompanist/pager/), la cual ya está integrada en `Jetpack Compose`.

El bloque indicador de desplazamiento lateral en la parte superior se basa en un ejemplo de la [documentación oficial](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#TabRow(kotlin.Int,androidx.compose.ui.Modifier,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,kotlin.Function1,kotlin.Function0,kotlin.Function0)), al cual se le han hecho modificaciones. Se han añadido algunos efectos de escala que hacen que parezca gelatina al moverse, logrando así un efecto de animación excelente; sería bastante difícil crear una animación de este tipo por cuenta propia.

Dado que el interior del `Pager` está implementado con `LazyRow`, al desplazarse hasta los bordes aparece un efecto de sombra que no resulta muy atractivo. Tras buscar ampliamente, se encontró una forma de eliminarlo: envolver el componente con un `CompositionLocalProvider(LocalOverscrollConfiguration provides null){}`; si se desea aplicar un efecto de sombra, basta con definir una `OverscrollConfiguration` e introducirla como parámetro.



## Módulo de mapa

### Fuente de datos

Los datos del módulo de mapas provienen de [OpenStreetMap](https://www.openstreetmap.org/), un proyecto de mapas de código abierto. Los miembros de la Asociación de Pioneros en Redes de la Universidad Politécnica de Beijing ya dibujaron allí mapas detallados del campus e incluso crearon un sitio web de mapas en línea llamado [A BIT of Map](https://map.bitnp.net/); agradecemos enormemente su contribución.

Sin embargo, en China existe el problema de contaminación DNS en `OpenStreetMap`, lo que impide que los clientes puedan acceder a ella de forma normal. Por eso, primero configuré un servicio de teselas de mapa utilizando [openstreetmap-tile-server](https://github.com/Overv/openstreetmap-tile-server) basado en `Docker` en el servidor, y desplegué los datos del mapa de la región de Pekín. Pero tras el despliegue descubrí que consumía demasiados recursos del servidor y era vulnerable a ataques, por lo que finalmente abandoné esta solución y opté por utilizar un proxy en el servidor para resolver el problema.

La solución final consiste en modificar primero el archivo `hosts` en el servidor para evitar la contaminación por DNS y, a continuación, crear una regla de proxy en `Nginx`. Cuando se accede a `https://map.bit101.flwfdd.xyz/tile/{z}/{x}/{y}.png`, la solicitud se redirige a `https://tile.openstreetmap.org/{z}/{x}/{y}.png`. Aunque la velocidad de acceso es algo lenta, como los datos del mapa prácticamente no cambian, se puede implementar caché en `Nginx`; de este modo, los datos de la zona de la universidad se obtienen básicamente desde el servidor local.

También hay que tener en cuenta que al consultar la API de `OpenStreetMap` es necesario incluir un `User-Agent`, de lo contrario la solicitud será bloqueada. Durante la depuración, noté que la página funcionaba correctamente, pero en Android nunca se cargaba adecuadamente; pasé mucho tiempo buscando el error.

### Implementación en Android

Encontré un proyecto de código abierto llamado [MapCompose](https://github.com/p-lr/MapCompose) que se adapta perfectamente a mis necesidades.

Sin embargo, existe un problema de visualización: el texto que muestra el componente del mapa es demasiado pequeño. Pero como el mapa se dibuja como una imagen de bits y no como vector, no es posible modificar directamente el tamaño del texto, por lo que se pensó en ampliar forzadamente el componente del mapa. Finalmente, al agregar `Modifier.fillMaxSize(0.5f).scale(2f)` al componente del mapa, se logra ampliarlo dos veces (equivalente a dibujar el mapa primero a la mitad de su tamaño y luego expandirlo a tamaño completo de pantalla); se aplica el mismo método para ampliarlo a otros múltiplos.

Además, para acelerar la velocidad de carga y permitir ver el mapa sin conexión, se agregó un `cacheControl` en `OkHttp` para implementar el caché.

## Módulo BIT101 en la página web

La implementación de la función de inicio de sesión automático en `BIT101` ya fue explicada anteriormente.

### Funciones implementadas mediante la supervisión de rutas

Cuando se detecta que la ruta cambia a la página de consulta de calificaciones, se ejecuta un fragmento de código `JavaScript` para lograr el relleno automático del número de estudiante y la contraseña.

Cuando se detecta que la ruta cambia a un sitio que no es `BIT101`, se interrumpe la solicitud en el `WebView` y se redirige al navegador del sistema para evitar problemas de manejo de situaciones complejas y cuestiones de seguridad.


### Problemas con el estado del WebView

Cada vez que se cambia la navegación dentro de la aplicación y se vuelve a la página donde se encuentra el `WebView`, se observa que su estado ha cambiado. Esto ocurre porque solo se utilizó `rememberWebViewState`; después de buscar mucho, se encontró [un issue](https://github.com/google/accompanist/pull/1557) en `GitHub`. Este problema se resolvió hace una o dos semanas con una nueva función `rememberSaveableWebViewState`: basta con actualizar la biblioteca a la versión `alpha` más reciente y pasar a utilizar `rememberSaveableWebViewState` para guardar estados simples.

Sin embargo, la implementación actual no restaura completamente el estado; solo vuelve a la página y posición de navegación anteriores (por ejemplo, se pierde el contenido de los campos de texto), pero por ahora sirve para usarlo de forma provisional.

### Implementación de la subida de archivos en WebView

Por defecto, `WebView` no admite la subida de archivos; es necesario reescribir manualmente la función `onShowFileChooser` de `WebChromeClient` para lograrlo. Primero definimos un `ActivityResultRegistry` en `Application`, luego lo configuramos como `ActivityResultContracts.GetMultipleContents()` en `MainActivity`, y finalmente transmitimos los resultados de la selección de archivos mediante un `MutableStateFlow` global.

Esta lógica es bastante compleja; como referencia principal se utilizó [este artículo](https://blog.csdn.net/LiePy/article/details/125797893).


## Funciones globales

### Animación de ocultamiento de la barra de navegación inferior

En algunas interfaces no queremos que aparezca la barra de navegación inferior. En `Material3`, dicha barra se define dentro del `Scaffold` de la función de composición de nivel superior, por lo que la configuración también solo puede realizarse a ese nivel. El método de implementación no es complicado: basta con crear un `State` basado en las rutas y luego envolver la barra de navegación con `AnimatedVisibility`. La implementación de animaciones en `Material3` sigue siendo sencilla, pero aquí surge otro problema: durante el cambio de barra de navegación ya se ha pasado a otra interfaz, y el `Padding` cambia antes y después del cambio, lo que provoca una vibración extraña en el diseño de la interfaz. En realidad, este `Padding` es transmitido por `NavHost` para que los nodos inferiores lo gestionen por sí mismos; por eso, aquellas interfaces que no necesitan barra de navegación pueden simplemente ignorar por completo el `Padding` recibido.

### Acerca del tema

Los colores visuales principales de `BIT101` son naranja con un tono de `24` (como <b style="color:#FF9A57">#FF9A57</b>) y azul con un tono de `192` (como <b style="color:#00ABD6">#00ABD6</b>).

Primero, se generó de forma sencilla un conjunto de archivos de colores («Color.kt», «Theme.kt») a través del sitio oficial [Material Theme Builder](https://m3.material.io/theme-builder), y posteriormente se ajustaron manualmente las combinaciones de colores en «Color.kt». Android12+ ofrece una «API» que permite generar colores dinámicamente según la configuración del sistema; por lo tanto, se desea permitir al usuario decidir si activar esta función, así como decidir si habilitar el modo oscuro. Estas configuraciones, una vez establecidas en la pantalla de ajustes, se guardarán en «DataStore», y luego se monitorearán editando «BIT101Theme» en el archivo «Theme.kt» para cambiar el tema cada vez que cambie el estado. Dado que «BIT101Theme» es el nodo raíz de toda la aplicación, cuando se modifica, también cambia el tema de toda la aplicación.

Para lograr una experiencia inmersiva, también se desea que los colores de la barra de estado del sistema en la parte superior y de la barra de navegación del sistema en la parte inferior se adapten a los colores de la aplicación. Esto se puede conseguir simplemente modificando `window.statusBarColor` y `window.navigationBarColor` dentro de la `Activity`. Además, cuando la barra de estado superior sea clara o oscura, es necesario cambiar simultáneamente el color del texto de dicha barra. Dado que estos colores deben adaptarse según los cambios en el tema, se incluye este código dentro de la función `BIT101Theme{}`; de esta manera, se ejecuta cada vez que cambia el tema durante el proceso de reestructuración, pero no en otras situaciones que podrían causar problemas de rendimiento.


## Algunos problemas adicionales

### Efecto de onda al hacer clic y forma del componente

En muchos casos, la forma de la onda se adapta automáticamente a la del componente, pero en situaciones específicas (como al usar `Modifier.clickable{}` para definir la respuesta al clic), la onda adopta una forma rectangular; en esos casos se puede utilizar `Modifier.clip()` para transmitir la forma del componente a la onda.

### Problema de cierre inesperado causado por rememberSaveable

Al utilizar `rememberSaveable` para guardar objetos personalizados, la aplicación se cierra abruptamente al cambiar de pantalla, ya que el proceso de serialización predeterminado no puede manejar dichos objetos al realizar la operación de guardado. La solución es definir manualmente las operaciones de guardado y recuperación del objeto, o bien usar directamente `remember` en lugar de `rememberSaveable`.

### Problemas al incluir la biblioteca Java

Los calendarios de LeXue están en formato `.ics`; utilicé la librería `iCal4j` para parsearlos, pero esta no cuenta con versión en `Kotlin`, por lo que solo pude incluir la versión en `Java`. Sin embargo, durante la compilación surgieron conflictos entre algunas librerías dependientes, y aunque probé muchos métodos sin éxito y los mensajes de error no eran claros, afortunadamente encontré una solución en un [repositorio de GitHub](https://github.com/bitfireAT/ical4android/blob/main/build.gradle): bastó con agregar algunas instrucciones `exclude` en `build.gradle` para excluir esas librerías conflictivas.


### Creación y muestra automática de la declaración de licencia de código abierto

En este proyecto se utilizan numerosas bibliotecas de terceros que aplican diversos acuerdos de licencia de código abierto. Por respeto al espíritu del código abierto, se debe añadir una función dentro de la aplicación para mostrar las bibliotecas de código abierto que se están utilizando.

Sin embargo, si se tiene que verificar manualmente las bibliotecas de código abierto utilizadas y sus respectivos acuerdos de licencia, esta tarea resultará muy engorrosa. Por eso utilicé un plugin de `gradle` de código abierto [gradle-license-plugin](https://github.com/jaredsburrows/gradle-license-plugin) para escanear automáticamente las bibliotecas de código abierto del proyecto, generar un archivo `txt` que se almacena en el directorio `assets`, y luego leer ese archivo en la aplicación para mostrarlo.

Al usarlo, basta con ejecutar la tarea `gradle licenseDebugReport` para generar un archivo `txt` que se guardará en `assets`; posteriormente, la aplicación puede leerlo y mostrarlo.

### Problemas de compresión y obfuscación durante el empaquetado

Durante el empaquetado, si se establece `minifyEnabled true` en `build.gradle`, el código se comprimirá y obfuscará, lo que permite reducir un paquete de instalación de aproximadamente 15 MB a sorprendentes 4 MB o más. Sin embargo, después de la instalación, algunas funciones pueden dejar de funcionar correctamente, ya que algunos paquetes dependientes utilizan características como el reflexión, las cuales no son compatibles con la compresión y obfuscación. Si aún se desea realizar esta compresión, es necesario configurar manualmente la exclusión de ciertos paquetes dependientes. Debido a las limitaciones de tiempo, y dado que el APK sin comprimir mide menos de 20 MB, se decidió no seguir adelante con ello por ahora; se podrá optimizar posteriormente.

### Uso de Compose BoM

`Jetpack Compose` se actualiza con gran rapidez, cuenta con numerosos paquetes y, además, existe una compleja red de requisitos de versión y dependencias entre ellos. Con frecuencia, es necesario cambiar la versión de algún paquete debido a necesidades funcionales, pero tras hacerlo, la compilación deja de funcionar.

Más tarde descubrí el `Compose BoM`, ¡es realmente una herramienta increíble! Con solo una línea de código se puede gestionar la versión de todos los paquetes de `Compose`.

### Principios de optimización del rendimiento de Compose

Basándome en la documentación oficial, algunos blogs y mi propia experiencia práctica, he resumido algunos principios que permiten optimizar la eficiencia de Compose:  
* No se debe ejecutar en las funciones de Compose el código cuyo estado no cambia; todo ese código debe colocarse en ViewModel, remember{} o LaunchedEffect, para evitar que se ejecute repetidamente durante la refactorización.  
* Principio de elevación de estado:  
*

¡Acabo de darme cuenta de repente de que el documento entregado no está completo aquí, uff!

## Puntos destacados del proyecto

* El tema elegido es práctico y tiene un gran valor aplicado.  
* Está escrito con `Kotlin` + `Jetpack Compose`, manteniéndose al día con las tendencias actuales.  
* La interfaz de usuario se construye con componentes de `Material Design 3`, aplicando los principios de diseño de `Material You`.  
* Se utilizan ampliamente tipos reactivos como `Flow` para manejar datos y estados, lo que garantiza rutas de transmisión claras.  
* Todas las operaciones intensivas en E/S se realizan dentro de corutinas, lo que mejora significativamente la eficiencia de ejecución.  
* Se han tenido en cuenta adecuadamente las cuestiones de seguridad para proteger la privacidad de los usuarios.  
* Se emplea `GitHub` para el desarrollo abierto y la gestión del código.  
* Se ha redactado documentación detallada, lo que permite a otros estudiantes contribuir al desarrollo y asegura la sostenibilidad del proyecto.

## Planes de desarrollo futuros

* Mostrar el calendario de clases en la línea del escritorio mediante widgets.  
* Mejorar la seguridad mediante reconocimiento biométrico.  
* Implementar el inicio de sesión con tarjeta universitaria usando la función NFC (viabilidad no verificada).  
* Gestionar las `Cookie` de forma encriptada.  
* Actualizaciones adicionales de las funcionalidades de la plataforma `BIT101`.
