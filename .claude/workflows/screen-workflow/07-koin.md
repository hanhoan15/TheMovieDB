# Step 6: Register ViewModel in Koin

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/di/ViewModelModule.kt`

## Current registrations

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

## How to add yours

### ViewModel with only repository dependencies
```kotlin
viewModel { MyNewViewModel(get()) }
```
- `get()` resolves `MovieRepository` from Koin automatically
- Works when constructor is: `class MyNewViewModel(private val repository: MovieRepository)`

### ViewModel with multiple dependencies
```kotlin
viewModel { MyNewViewModel(get(), get()) }
```
- Each `get()` resolves a different type: first `MovieRepository`, second `WatchListRepository`
- Koin matches by type, not by position

### ViewModel with route parameter
```kotlin
viewModel { params -> MyNewViewModel(params.get(), get(), get()) }
```
- `params.get()` gets the navigation argument (e.g., `movieId: Int`)
- Remaining `get()` calls resolve repositories
- Works when constructor is: `class MyNewViewModel(private val movieId: Int, private val repository: MovieRepository, ...)`

## How Koin passes the route parameter

In `AppNavGraph.kt`, when the ViewModel needs a route param, Koin receives it automatically through `koinViewModel()`. The navigation system passes route parameters to Koin's `params`.

Screen side (no extra code needed):
```kotlin
viewModel: DetailViewModel = koinViewModel()    // Koin handles everything
```

## Import needed
```kotlin
import org.koin.core.module.dsl.viewModel
```

## How `get()` resolves dependencies

Koin looks up the type in its registry:
```
get()  →  looks for MovieRepository  →  finds TmdbMovieRepository (registered in RepositoryModule)
get()  →  looks for WatchListRepository  →  finds WatchListRepository singleton (registered in RepositoryModule)
get()  →  looks for TmdbApiService  →  finds TmdbApiService (registered in NetworkModule)
```

The full dependency chain:
```
ViewModelModule                  RepositoryModule               NetworkModule
viewModel { HomeViewModel(       single<MovieRepository> {       single { TmdbApiService(
    get() ←───────────────────── TmdbMovieRepository(                get() ←──── HttpClient
) }                                  get() ←────────────────────── )
                                 ) }                             }
```

---

## Why these choices?

### Why Koin over alternatives?

| DI Framework | KMP support | Setup style | Verdict |
|-------------|-------------|-------------|---------|
| **Koin** | Full (all platforms) | Runtime DSL (`module { }`) | **Use this** |
| Hilt/Dagger | Android-only | Annotation processing (kapt/ksp) | Can't use in KMP |
| Kodein | Full | DSL, similar to Koin | Less popular, fewer resources |
| Manual DI | Full | Constructor calls everywhere | Boilerplate, no lifecycle mgmt |

Koin works across all Kotlin targets with zero annotation processing:
- `module { single { }, viewModel { } }` — pure Kotlin DSL
- No code generation step (faster builds than Hilt)
- `viewModel { }` DSL integrates directly with Compose Navigation

### Why `viewModel { }` DSL (not `single { }`)?
```kotlin
viewModel { HomeViewModel(get()) }      // lifecycle-aware
// NOT:
single { HomeViewModel(get()) }          // singleton — never destroyed
```
- `viewModel { }` creates ViewModel instances tied to the navigation back stack entry
- When the screen is popped, the ViewModel is destroyed (cleanup)
- `single { }` would keep the ViewModel alive forever — memory leak
- Koin's `viewModel { }` integrates with `koinViewModel()` in Compose

### Why `get()` resolves by type (not by name)?
```kotlin
viewModel { HomeViewModel(get()) }
// get() → looks for MovieRepository type → finds TmdbMovieRepository
```
- Type-based resolution is safe: wrong type = compile error
- Named resolution (`get(named("movieRepo"))`) is error-prone: wrong string = runtime crash
- Multiple instances of the same type are rare — when needed, use `named()` qualifier

### Why `params.get()` for route arguments (not `get()`)?
```kotlin
viewModel { params -> DetailViewModel(params.get(), get(), get()) }
//                                     ^^^^^^^^^^^
//                                     route arg (movieId: Int)
```
- `params.get()` extracts the navigation argument passed by the route
- `get()` resolves from Koin's dependency graph (repositories, services)
- Mixing them up causes runtime errors: `params.get()` for DI deps → `NoParameterFoundException`
