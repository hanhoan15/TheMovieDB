---
name: setup-di
description: Step-by-step guide for registering and injecting dependencies with Koin in this KMP project
user-invocable: true
argument-hint: "<what to register, e.g. ViewModel, Repository, Service>"
---

# Flow: Dependency Injection with Koin

Register and inject: **$ARGUMENTS**

This project uses Koin 4.0.4 for DI. All modules are in `core/di/`.

---

## Module Structure

```
core/di/
├── NetworkModule.kt      ← HttpClient, Json, TmdbApiService
├── RepositoryModule.kt   ← MovieRepository, WatchListRepository
├── ViewModelModule.kt    ← All ViewModels
└── AppModules.kt         ← Aggregates all modules into one list
```

---

## How to: Register a new ViewModel

**File:** `core/di/ViewModelModule.kt`

```kotlin
val viewModelModule = module {
    // SIMPLE: no route params, all deps auto-resolved
    viewModelOf(::MyNewViewModel)

    // WITH ROUTE PARAMS: pass navigation args manually
    viewModel { params ->
        DetailViewModel(
            movieId = params.get(),     // from navigation route
            repository = get(),         // auto-resolved by Koin
            watchListRepository = get(),
        )
    }
}
```

**Key rules:**
- `viewModelOf(::ClassName)` — Koin auto-resolves all constructor parameters
- `viewModel { params -> }` — use when the ViewModel needs a navigation argument
- `params.get()` retrieves the first unmatched parameter from the caller
- `get()` resolves a registered dependency by type
- ViewModel is injected in Screen via `koinViewModel()`:
  ```kotlin
  @Composable
  fun MyScreen(
      viewModel: MyViewModel = koinViewModel(),
  )
  ```

**Existing registrations:**
```kotlin
viewModelOf(::HomeViewModel)
viewModel { params -> DetailViewModel(params.get(), get(), get()) }
viewModelOf(::SearchViewModel)
viewModelOf(::WatchListViewModel)
```

---

## How to: Register a new Repository

**File:** `core/di/RepositoryModule.kt`

```kotlin
val repositoryModule = module {
    // INTERFACE BINDING: register implementation for interface
    singleOf(::TmdbMovieRepository).bind<MovieRepository>()

    // SINGLETON: single instance shared across the app
    single { WatchListRepository() }

    // NEW REPOSITORY:
    singleOf(::MyNewRepository).bind<MyNewRepositoryInterface>()
}
```

**Key rules:**
- `singleOf(::Impl).bind<Interface>()` — register implementation and bind to interface type
- `single { }` — manual singleton creation
- `singleOf(::Class)` — auto-resolve constructor, single instance
- Repositories should be `single` (one instance) since they hold state or caches
- The interface is what gets injected into ViewModels (not the implementation)

**Existing registrations:**
```kotlin
singleOf(::TmdbMovieRepository).bind<MovieRepository>()
single { WatchListRepository() }
```

---

## How to: Register a new API Service or Network dependency

**File:** `core/di/NetworkModule.kt`

```kotlin
val networkModule = module {
    // JSON serializer (reusable)
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    // HTTP Client with auth and JSON
    single {
        createPlatformHttpClient().config {
            install(ContentNegotiation) {
                json(get<Json>())
            }
            defaultRequest {
                header("Authorization", "Bearer ${ApiConstants.ACCESS_TOKEN}")
                header("accept", "application/json")
            }
        }
    }

    // API Service
    singleOf(::TmdbApiService)

    // NEW SERVICE:
    single { MyNewApiService(get()) }  // get() resolves HttpClient
}
```

**Key rules:**
- HttpClient is platform-specific via `createPlatformHttpClient()` (expect/actual)
- Auth header is set globally in `defaultRequest` — all requests get it automatically
- JSON config: `ignoreUnknownKeys = true` so new API fields don't crash the app
- `ContentNegotiation` plugin auto-serializes/deserializes JSON bodies

---

## How to: Add a new Koin module

**File:** `core/di/AppModules.kt`

```kotlin
val appModules = listOf(
    networkModule,
    repositoryModule,
    viewModelModule,
    myNewModule,          // ← add here
)
```

**This is the only list that matters.** Both Android and iOS init Koin with `appModules`.

---

## How to: Inject in different contexts

### In a Composable (Screen):
```kotlin
// ViewModel injection (most common):
@Composable
fun MyScreen(viewModel: MyViewModel = koinViewModel()) { }

// Direct dependency injection (rare, prefer ViewModel):
@Composable
fun MyScreen() {
    val repository = koinInject<MovieRepository>()
}
```

### In a ViewModel (constructor injection):
```kotlin
class MyViewModel(
    private val repository: MovieRepository,      // auto-injected by Koin
    private val watchList: WatchListRepository,   // auto-injected by Koin
) : ViewModel()
```

### In a Repository (constructor injection):
```kotlin
class MyRepository(
    private val apiService: TmdbApiService,  // auto-injected by Koin
) : MyRepositoryInterface
```

---

## Platform initialization

### Android (`MainActivity.kt`):
```kotlin
if (GlobalContext.getOrNull() == null) {
    startKoin {
        androidContext(applicationContext)  // requires koin-android dependency
        modules(appModules)
    }
}
```

### iOS (`MainViewController.kt`):
```kotlin
if (runCatching { KoinPlatform.getKoin() }.isFailure) {
    startKoin {
        modules(appModules)
    }
}
```

**Key rules:**
- Both platforms check if Koin is already initialized before calling `startKoin`
- Android needs `androidContext()` for Android-specific features
- `appModules` is the single source of truth for all DI configuration
- Never call `startKoin` twice — it will crash

---

## Dependency graph

```
NetworkModule
├── Json
├── HttpClient (uses Json, platform engine)
└── TmdbApiService (uses HttpClient)

RepositoryModule
├── TmdbMovieRepository (uses TmdbApiService) → binds MovieRepository
└── WatchListRepository (standalone singleton)

ViewModelModule
├── HomeViewModel (uses MovieRepository)
├── DetailViewModel (uses movieId param, MovieRepository, WatchListRepository)
├── SearchViewModel (uses MovieRepository)
└── WatchListViewModel (uses WatchListRepository)
```

---

## Common mistakes to avoid

1. **Forgetting to add module to `appModules`** — Koin won't find your dependency
2. **Using `factory` instead of `single` for repositories** — creates new instance each time, loses state
3. **Injecting implementation instead of interface** — always bind to interface: `singleOf(::Impl).bind<Interface>()`
4. **Circular dependencies** — if A depends on B and B depends on A, use `lazy { get<A>() }`
5. **Missing `koin-android` dependency** — needed for `androidContext()` on Android
