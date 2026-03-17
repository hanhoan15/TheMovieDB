# Step 6: Dependency Injection — Best Practices

This project uses **Koin 4.0.4** for DI. This file covers how every layer is wired together and best practices for adding new dependencies.

---

## Module architecture

```
core/di/
├── NetworkModule.kt       ← Layer 1: HTTP + JSON + API service
├── RepositoryModule.kt    ← Layer 2: Repositories (use services from Layer 1)
├── ViewModelModule.kt     ← Layer 3: ViewModels (use repositories from Layer 2)
└── AppModules.kt          ← Aggregates all modules
```

---

## Layer 1: NetworkModule — HTTP infrastructure

```kotlin
package com.example.themoviedb.core.di

import com.example.themoviedb.core.data.remote.ApiConstants
import com.example.themoviedb.core.data.remote.TmdbApiService
import com.example.themoviedb.core.data.remote.createPlatformHttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    // 1. JSON serializer — reused by HttpClient
    single {
        Json {
            ignoreUnknownKeys = true    // API adds fields → no crash
            isLenient = true            // tolerates minor JSON issues
            coerceInputValues = true    // null for non-nullable → use default
        }
    }

    // 2. HttpClient — platform engine + plugins
    single {
        createPlatformHttpClient().config {
            install(ContentNegotiation) {
                json(get<Json>())           // ← uses Json from above
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                headers.append("Authorization", "Bearer ${ApiConstants.ACCESS_TOKEN}")
                headers.append("accept", "application/json")
            }
        }
    }

    // 3. API Service — uses HttpClient
    single { TmdbApiService(get()) }        // get() → HttpClient
}
```

### Platform HttpClient (expect/actual)

```kotlin
// commonMain — declaration only:
expect fun createPlatformHttpClient(): HttpClient

// androidMain — OkHttp engine:
actual fun createPlatformHttpClient(): HttpClient = HttpClient(OkHttp)

// iosMain — Darwin engine:
actual fun createPlatformHttpClient(): HttpClient = HttpClient(Darwin)
```

### Adding a new API service
```kotlin
// In NetworkModule:
single { MyNewApiService(get()) }   // get() → HttpClient (shared)
```

---

## Layer 2: RepositoryModule — Data access

```kotlin
package com.example.themoviedb.core.di

import com.example.themoviedb.core.data.repository.MovieRepository
import com.example.themoviedb.core.data.repository.TmdbMovieRepository
import com.example.themoviedb.core.data.repository.WatchListRepository
import org.koin.dsl.module

val repositoryModule = module {
    // Interface binding: register impl, bind to interface type
    single<MovieRepository> { TmdbMovieRepository(apiService = get()) }

    // Standalone singleton: no interface needed
    single { WatchListRepository() }
}
```

### Best practices for repositories

**Use `single` (not `factory`):**
```kotlin
single<MovieRepository> { TmdbMovieRepository(get()) }   // ONE instance, shared
// NOT: factory { TmdbMovieRepository(get()) }            // would create new instance each time
```
Repositories hold state (caches, in-memory lists). Multiple instances = data inconsistency.

**Bind to interface:**
```kotlin
single<MovieRepository> { TmdbMovieRepository(get()) }
```
ViewModels depend on `MovieRepository` interface, not `TmdbMovieRepository` class.
This enables swapping implementations (e.g., fake for testing).

**Standalone singleton (no interface):**
```kotlin
single { WatchListRepository() }
```
Used when there's only one implementation and no need for abstraction.
`WatchListRepository` is shared between `DetailViewModel` and `WatchListViewModel`.

### Adding a new repository
```kotlin
// With interface:
single<NewRepository> { NewRepositoryImpl(apiService = get()) }

// Without interface:
single { NewRepository() }
```

---

## Layer 3: ViewModelModule — Screen logic

```kotlin
package com.example.themoviedb.core.di

import com.example.themoviedb.feature.detail.DetailViewModel
import com.example.themoviedb.feature.home.HomeViewModel
import com.example.themoviedb.feature.search.SearchViewModel
import com.example.themoviedb.feature.watchlist.WatchListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { params -> DetailViewModel(params.get(), get(), get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { WatchListViewModel(get()) }
}
```

### How `get()` resolves types

Koin matches by type automatically:
```
get() in HomeViewModel(get())
  → looks for MovieRepository
  → finds single<MovieRepository> { TmdbMovieRepository(...) }
  → returns TmdbMovieRepository instance

get(), get() in DetailViewModel(params.get(), get(), get())
  → first get(): MovieRepository → TmdbMovieRepository
  → second get(): WatchListRepository → WatchListRepository singleton
```

### How `params` works for navigation arguments

```kotlin
// Registration:
viewModel { params -> DetailViewModel(params.get(), get(), get()) }
//                                     ^^^^^^^^^^^
//                                     movieId: Int from navigation

// The Screen doesn't pass it explicitly — Koin+Navigation handle it:
viewModel: DetailViewModel = koinViewModel()   // in Screen composable
```

### Adding a new ViewModel

**No route params:**
```kotlin
viewModel { MyNewViewModel(get()) }
// Constructor: class MyNewViewModel(private val repo: MovieRepository)
```

**With route params:**
```kotlin
viewModel { params -> MyNewViewModel(params.get(), get()) }
// Constructor: class MyNewViewModel(private val itemId: Int, private val repo: MovieRepository)
```

**Multiple dependencies:**
```kotlin
viewModel { MyNewViewModel(get(), get(), get()) }
// Constructor: class MyNewViewModel(
//     private val movieRepo: MovieRepository,
//     private val watchListRepo: WatchListRepository,
//     private val newRepo: NewRepository,
// )
```

---

## AppModules — Aggregation

```kotlin
package com.example.themoviedb.core.di

val appModules = listOf(
    networkModule,
    repositoryModule,
    viewModelModule,
)
```

**If you create a new module**, add it here:
```kotlin
val appModules = listOf(
    networkModule,
    repositoryModule,
    viewModelModule,
    myNewModule,           // ← add here
)
```

---

## Platform initialization

### Android (MainActivity.kt)
```kotlin
if (GlobalContext.getOrNull() == null) {
    startKoin {
        androidContext(applicationContext)
        modules(appModules)
    }
}
```

### iOS (MainViewController.kt)
```kotlin
if (runCatching { KoinPlatform.getKoin() }.isFailure) {
    startKoin {
        modules(appModules)
    }
}
```

Both check if Koin is already running before initializing (prevents double-init crashes).

---

## Complete dependency graph

```
NetworkModule
│
├── Json (single)
│
├── HttpClient (single)
│   ├── uses Json for ContentNegotiation
│   ├── uses createPlatformHttpClient() for engine
│   └── configures auth headers from ApiConstants
│
└── TmdbApiService (single)
    └── uses HttpClient

RepositoryModule
│
├── TmdbMovieRepository (single, binds MovieRepository)
│   └── uses TmdbApiService
│
└── WatchListRepository (single, standalone)

ViewModelModule
│
├── HomeViewModel → MovieRepository
├── DetailViewModel → movieId (param) + MovieRepository + WatchListRepository
├── SearchViewModel → MovieRepository
└── WatchListViewModel → WatchListRepository
```

---

## Common mistakes

| Mistake | Symptom | Fix |
|---------|---------|-----|
| Forgot to add module to `appModules` | `No definition found for class 'X'` | Add to `AppModules.kt` list |
| Used `factory` for repository | Data disappears between screens | Change to `single` |
| Didn't bind to interface | `No definition found for class 'MovieRepository'` | Use `single<Interface> { Impl() }` |
| Missing import in ViewModelModule | `Unresolved reference` | Add import for ViewModel class |
| Circular dependency | Stack overflow at startup | Refactor to remove cycle, or use `lazy { get<T>() }` |
| Called `startKoin` twice | `KoinAppAlreadyStartedException` | Add `GlobalContext.getOrNull() == null` guard |

---

## Why these choices?

### Why 3 separate modules (not one big module)?

| Approach | Pros | Cons | Verdict |
|----------|------|------|---------|
| **Layered modules** (Network, Repository, ViewModel) | Clear separation, easy to find registrations, can swap layers independently | More files | **Use this** |
| Single `appModule` | Less files, simple | Hard to find things, can't swap layers | Only for tiny apps |
| Feature-based modules | Grouped by feature | Cross-feature deps get messy, repositories are shared | Avoid |

Layered modules match the dependency flow: Network → Repository → ViewModel. Each layer only depends on layers below it. Testing can swap just RepositoryModule with a test module.

### Why `single<Interface> { Impl() }` (not `single { Impl() }`)?
```kotlin
single<MovieRepository> { TmdbMovieRepository(get()) }   // binds to interface
// NOT:
single { TmdbMovieRepository(get()) }                     // only findable as TmdbMovieRepository
```
- ViewModels depend on `MovieRepository` (interface) → `get()` must find it by interface type
- Without `<MovieRepository>`, Koin registers as `TmdbMovieRepository` → ViewModels can't find it
- Enables swapping: test module can bind `FakeMovieRepository` to the same `MovieRepository` type

### Why constructor injection (not Koin `inject()`)?
```kotlin
// Our pattern — constructor injection:
class TmdbMovieRepository(private val apiService: TmdbApiService) : MovieRepository

// Alternative — Koin inject:
class TmdbMovieRepository : MovieRepository, KoinComponent {
    private val apiService: TmdbApiService by inject()
}
```
Constructor injection is better because:
- Dependencies are explicit in the constructor signature
- Testable without Koin: `TmdbMovieRepository(fakeApiService)`
- No `KoinComponent` interface needed (cleaner)
- Missing dependency = compile error (not runtime crash)
