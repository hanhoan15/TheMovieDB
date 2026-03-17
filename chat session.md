# TheMovieDB - Kotlin Multiplatform Movie App

## Project Overview
KMP app targeting Android + iOS using Compose Multiplatform, Ktor, Koin, and Compose Navigation.

## Quick Commands
- **Build Android:** `./gradlew composeApp:assembleDebug`
- **Build iOS:** `./gradlew composeApp:compileKotlinIosSimulatorArm64`
- **Run tests:** `./gradlew composeApp:testDebugUnitTest`
- **Full check:** `./gradlew composeApp:compileDebugKotlinAndroid composeApp:compileKotlinIosSimulatorArm64 composeApp:testDebugUnitTest`

## Tech Stack
| Layer | Technology |
|-------|-----------|
| UI | Compose Multiplatform 1.10.0, Material3 |
| Navigation | Compose Navigation 2.9.0-alpha14 (serializable routes) |
| Networking | Ktor Client 3.1.0 (OkHttp on Android, Darwin on iOS) |
| DI | Koin 4.0.4 |
| Serialization | kotlinx-serialization 1.8.1 |
| Image loading | Coil 3.4.0 with Ktor backend |
| State | ViewModel + StateFlow (MVVM) |
| Testing | kotlin-test + kotlinx-coroutines-test |

## Architecture
- **Pattern:** MVVM with Repository pattern
- **Source sets:** `commonMain` (shared), `androidMain`, `iosMain`
- **Feature modules:** `feature/{home,detail,search,watchlist,imageviewer,webview}/`
- **Shared core:** `core/{data,di,navigation,ui}/`
- **Platform-specific code:** Use `expect`/`actual` declarations (see `HttpClientFactory`, `InAppWebView`)

## Key Conventions
- Every feature has its own `Screen.kt` + `ViewModel.kt`
- ViewModels use `MutableStateFlow` + `StateFlow` for UI state (no LiveData)
- DI: Register all ViewModels in `core/di/ViewModelModule.kt`, repositories in `RepositoryModule.kt`
- Navigation routes are `@Serializable` data objects/classes in `core/navigation/AppRoutes.kt`
- Domain models live in `core/data/model/`, DTOs in `core/data/model/dto/`
- DTO-to-domain mapping via extension functions in `core/data/mapper/MovieMapper.kt`
- Reusable composables go in `core/ui/components/`
- Theme constants in `core/ui/theme/` (AppColors, AppTypography, Dimensions)

## Workflow Guides (detailed step-by-step docs)
Complete reference with real code from this project:

### Screen Workflow — `.claude/workflows/screen-workflow/`
How to create a new screen end-to-end (8 steps):
1. @.claude/workflows/screen-workflow/01-overview.md — Overview + file map
2. @.claude/workflows/screen-workflow/02-uistate.md — Define UiState data class
3. @.claude/workflows/screen-workflow/03-viewmodel.md — Create ViewModel (with real examples)
4. @.claude/workflows/screen-workflow/04-screen.md — Build Screen composable
5. @.claude/workflows/screen-workflow/05-route.md — Add navigation route
6. @.claude/workflows/screen-workflow/06-navgraph.md — Wire up in NavGraph
7. @.claude/workflows/screen-workflow/07-koin.md — Register in Koin DI
8. @.claude/workflows/screen-workflow/08-test.md — Write unit tests
9. @.claude/workflows/screen-workflow/09-verify.md — Build and verify

### Screen Pattern Workflows — `.claude/workflows/screen-patterns/`
Deep-dive guides for each screen type (layout blueprints, component choices, API patterns, alternatives):
- @.claude/workflows/screen-patterns/list-grid-screen.md — Grid + pagination + tabs (HomeScreen pattern)
- @.claude/workflows/screen-patterns/detail-screen.md — Aggregated detail + sections + bookmark (DetailScreen pattern)
- @.claude/workflows/screen-patterns/search-screen.md — Debounced search + animated states (SearchScreen pattern)
- @.claude/workflows/screen-patterns/local-list-screen.md — Reactive local data + empty state (WatchListScreen pattern)
- @.claude/workflows/screen-patterns/fullscreen-viewer-screen.md — Full-screen pager, no ViewModel (ImageViewerScreen pattern)
- @.claude/workflows/screen-patterns/webview-screen.md — Platform web content, expect/actual (WebViewScreen pattern)

### API Workflow — `.claude/workflows/api-workflow/`
How to add a new API endpoint with DI best practices (9 steps):
1. @.claude/workflows/api-workflow/01-overview.md — Overview + data flow diagram
2. @.claude/workflows/api-workflow/02-dto.md — Create @Serializable DTO
3. @.claude/workflows/api-workflow/03-domain-model.md — Create domain model
4. @.claude/workflows/api-workflow/04-mapper.md — Write DTO → Domain mapper
5. @.claude/workflows/api-workflow/05-api-service.md — Add Ktor API call
6. @.claude/workflows/api-workflow/06-repository.md — Add to repository (interface + impl)
7. @.claude/workflows/api-workflow/07-dependency-injection.md — Koin DI best practices
8. @.claude/workflows/api-workflow/08-test-fake.md — Update test fake
9. @.claude/workflows/api-workflow/09-mapper-test.md — Write mapper tests
10. @.claude/workflows/api-workflow/10-verify.md — Build and verify

## Slash Commands (skills)
Quick-action skills for common tasks:
- `/create-screen <Name>` — Full feature: ViewModel + Screen + DI + Route + NavGraph + Test
- `/create-api <Name>` — API endpoint: DTO + ApiService method + Mapper + Repository
- `/create-repository <Name>` — Repository: Interface + Impl + DI + Test fake
- `/create-dto-mapper <Name>` — Data models: DTO + Domain model + Mapper + Tests
- `/create-navigation <Name>` — Navigation: Route + NavGraph + Transitions
- `/create-test <Name>` — Unit tests: ViewModel, mapper, or repository tests
- `/setup-di <Name>` — Koin DI: How to register and inject any dependency
- `/build [android|ios|all]` — Build and report errors
- `/test [TestClass]` — Run tests and report results
- `/new-feature <Name>` — Quick scaffold
- `/fix-bug <description>` — Investigate and fix a bug
- `/review-code [file]` — Code review against conventions
- `/add-component <Name>` — New reusable Compose component

## Rules
@.claude/rules/kotlin.md
@.claude/rules/testing.md
@.claude/rules/architecture.md

To continue this session, run:
codex resume 019c9436-a3dd-7f93-9301-d28d14c14309
Resume this session with:                                                       
claude --resume cf16a85a-d8b9-4c7c-92b8-47ef3865c654
