# Screen Creation Workflow — Overview

## What you will create
Every screen in this app follows MVVM with these files:

```
1. UiState data class       → Defines what the screen displays
2. ViewModel                → Manages state, calls repository
3. Screen composable        → Renders UI from UiState
4. Navigation route         → Type-safe route with @Serializable
5. NavGraph wiring          → Connects route to screen composable
6. Koin DI registration     → Makes ViewModel injectable
7. Unit test                → Tests ViewModel logic
```

## File locations
```
composeApp/src/commonMain/kotlin/com/example/themoviedb/
├── feature/<name>/
│   ├── <Name>ViewModel.kt          ← Steps 1-2
│   └── <Name>Screen.kt             ← Step 3
├── core/navigation/
│   ├── AppRoutes.kt                ← Step 4
│   └── AppNavGraph.kt              ← Step 5
├── core/di/
│   └── ViewModelModule.kt          ← Step 6
composeApp/src/commonTest/kotlin/com/example/themoviedb/
└── feature/<Name>ViewModelTest.kt   ← Step 7
```

## Workflow steps (detailed in separate files)
1. [02-uistate.md](./02-uistate.md) — Define the UiState data class
2. [03-viewmodel.md](./03-viewmodel.md) — Create the ViewModel
3. [04-screen.md](./04-screen.md) — Build the Screen composable
4. [05-route.md](./05-route.md) — Add a navigation route
5. [06-navgraph.md](./06-navgraph.md) — Wire up in AppNavGraph
6. [07-koin.md](./07-koin.md) — Register ViewModel in Koin
7. [08-test.md](./08-test.md) — Write unit tests
8. [09-verify.md](./09-verify.md) — Build and verify

## Which screen pattern should I use?

Before following the generic steps below, identify which pattern your screen matches. Each pattern has a dedicated workflow with layout blueprints, component choices, and API patterns:

| My screen needs... | Pattern | Workflow file |
|--------------------|---------|----|
| Multi-column grid + pagination + tab filtering | **List Grid** | [screen-patterns/list-grid-screen.md](../screen-patterns/list-grid-screen.md) |
| Single item detail loaded by ID + sections + bookmark | **Detail** | [screen-patterns/detail-screen.md](../screen-patterns/detail-screen.md) |
| Text input + debounced API search + animated states | **Search** | [screen-patterns/search-screen.md](../screen-patterns/search-screen.md) |
| Local/in-memory data list + empty state + reactive updates | **Local List** | [screen-patterns/local-list-screen.md](../screen-patterns/local-list-screen.md) |
| Full-screen media pager + no ViewModel + JSON route params | **Full-Screen Viewer** | [screen-patterns/fullscreen-viewer-screen.md](../screen-patterns/fullscreen-viewer-screen.md) |
| Platform web content + expect/actual + no ViewModel | **WebView** | [screen-patterns/webview-screen.md](../screen-patterns/webview-screen.md) |

**Decision shortcuts:**
- Has bottom nav? → List Grid, Search, or Local List
- No ViewModel needed? → Full-Screen Viewer or WebView
- Receives route parameter (ID)? → Detail
- Has text input? → Search

## Real examples in codebase
| Screen | Simple list | Detail with params | Search with debounce |
|--------|-------------|-------------------|---------------------|
| Files | `feature/home/` | `feature/detail/` | `feature/search/` |
| ViewModel deps | `MovieRepository` | `movieId`, `MovieRepository`, `WatchListRepository` | `MovieRepository` |
| Route type | `data object Home` | `data class Detail(val movieId: Int)` | `data object Search` |
| Koin registration | `viewModel { HomeViewModel(get()) }` | `viewModel { params -> DetailViewModel(params.get(), get(), get()) }` | `viewModel { SearchViewModel(get()) }` |
