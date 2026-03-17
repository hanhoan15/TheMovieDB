# Pattern: WebView Screen

> Reference implementation: `feature/webview/WebViewScreen.kt`

## When to use

- Screen displays **external web content** (URLs, embedded pages, videos)
- Uses **platform-specific rendering** via `expect`/`actual`
- Has **no ViewModel** — stateless composable with URL from route
- Has **no bottom nav** — sub-screen accessed via push navigation
- Minimal wrapper: just a top bar + the web view

---

## File checklist

```
feature/<name>/
└── <Name>Screen.kt                          ← Stateless composable (NO ViewModel)
core/ui/components/InAppWebView.kt            ← expect declaration (commonMain)
androidMain/.../InAppWebView.android.kt       ← actual: Android WebView
iosMain/.../InAppWebView.ios.kt               ← actual: WKWebView
core/navigation/AppRoutes.kt                  ← data class with url: String param
core/navigation/AppNavGraph.kt                ← composable with slide transitions
```

**No ViewModel, no Koin registration, no test file.**

---

## Layout blueprint

```
Column (fillMaxSize, background = ScreenBackground)
│
├── ScreenTopBar (title = "Trailer", onBack)
│
└── InAppWebView (url, fillMaxSize)
    ├── Android: android.webkit.WebView
    └── iOS: WKWebView via UIKitView
```

This is the simplest screen pattern in the app — two composables in a `Column`.

---

## Component choices

| Component | Why | Alternative | When to switch |
|-----------|-----|-------------|---------------|
| `ScreenTopBar` | Reusable header with back button + title | Custom `Row` | Never — `ScreenTopBar` is the standard |
| `InAppWebView` | Platform-specific web rendering | Chrome Custom Tab (Android) / SFSafariViewController (iOS) | When you want the system browser UI (address bar, back/forward) |
| `Column` | Simple vertical stack | `Scaffold` | When you need bottom bar or FAB slots |

### Why `InAppWebView` (not system browser)

| Approach | User experience | KMP pattern |
|----------|----------------|-------------|
| **`InAppWebView` (expect/actual)** | Stays in app, custom top bar, seamless | `expect fun InAppWebView()` in commonMain |
| Chrome Custom Tab / SFSafariViewController | Slides up system browser UI, address bar visible | Platform-specific launch, no Compose integration |
| External browser (`openUrl()`) | Leaves the app entirely | Simplest but worst UX |

`InAppWebView` keeps the user in the app. The trailer plays inside the app with a simple back button — no browser chrome, no context switch.

---

## Platform-specific implementation (expect/actual)

### Common declaration
```kotlin
// commonMain — core/ui/components/InAppWebView.kt
@Composable
expect fun InAppWebView(url: String, modifier: Modifier = Modifier)
```

### Android implementation
```kotlin
// androidMain
@Composable
actual fun InAppWebView(url: String, modifier: Modifier) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        },
        modifier = modifier,
    )
}
```

### iOS implementation
```kotlin
// iosMain
@Composable
actual fun InAppWebView(url: String, modifier: Modifier) {
    UIKitView(
        factory = {
            val webView = WKWebView()
            val request = NSURLRequest(uRL = NSURL(string = url)!!)
            webView.loadRequest(request)
            webView
        },
        modifier = modifier,
    )
}
```

**Key point:** `AndroidView` wraps Android views in Compose. `UIKitView` wraps UIKit views. Both are Compose Multiplatform APIs for embedding platform-native views.

---

## No ViewModel — stateless pattern

```kotlin
@Composable
fun <Name>Screen(
    url: String,           // from navigation route
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground),
    ) {
        ScreenTopBar(title = "Trailer", onBack = onBack)
        InAppWebView(url = url, modifier = Modifier.fillMaxSize())
    }
}
```

**Why no ViewModel?**
- No API calls — URL arrives from route
- No state management — WebView handles its own loading/rendering
- No business logic — pure URL display
- WebView progress/loading states are handled internally by the platform WebView

**When to add a ViewModel:** If you need to track loading progress, handle errors, inject auth tokens, or manage WebView navigation state (back/forward history).

---

## Navigation wiring

```kotlin
// AppRoutes.kt
@Serializable
data class <Name>(val url: String) : AppRoutes

// Navigating TO the web screen (from DetailScreen):
onOpenLink = { url ->
    navController.navigate(AppRoutes.<Name>(url = url))
}

// AppNavGraph.kt — slide transition (push navigation)
composable<AppRoutes.<Name>> { backStackEntry ->
    val route = backStackEntry.toRoute<AppRoutes.<Name>>()
    <Name>Screen(
        url = route.url,
        onBack = { navController.popBackStack() },
    )
}
```

**URL encoding note:** Compose Navigation handles URL-encoding of String params automatically. You don't need to manually encode/decode URLs with special characters.

---

## Extending this pattern

### Adding loading indicator
```kotlin
@Composable
fun <Name>Screen(url: String, onBack: () -> Unit) {
    var isLoading by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.ScreenBackground)) {
        ScreenTopBar(title = "Trailer", onBack = onBack)
        Box(modifier = Modifier.fillMaxSize()) {
            InAppWebView(
                url = url,
                onPageFinished = { isLoading = false },
                modifier = Modifier.fillMaxSize(),
            )
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}
```
This requires updating the `expect`/`actual` declarations to accept an `onPageFinished` callback.

### Adding JavaScript bridge
For Android, add `addJavascriptInterface()` in the `WebView.apply {}` block. For iOS, use `WKScriptMessageHandler`. This enables communication between the web page and the app.

---

## Common pitfalls

| Pitfall | Symptom | Fix |
|---------|---------|-----|
| Forgetting `javaScriptEnabled = true` | YouTube embeds don't play (Android) | `settings.javaScriptEnabled = true` |
| Missing `domStorageEnabled` | Some sites fail to load (Android) | `settings.domStorageEnabled = true` |
| Using `Scaffold` for minimal screen | Unnecessary complexity | Use `Column` — no bottom bar or FAB needed |
| Not using `expect`/`actual` | Platform-specific code in commonMain | Declare `expect` in commonMain, `actual` per platform |
| Hardcoding title | Can't reuse for different content types | Pass title as a route param or derive from URL |
| WebView in `LazyColumn` | Measurement issues — WebView height unknown | Use `fillMaxSize` in a fixed-size container |
