# Architecture Rules

## Layer Structure
```
UI (Screen.kt) → ViewModel → Repository → ApiService → Ktor HttpClient
```
- Screens observe `ViewModel.uiState: StateFlow<UiState>`
- ViewModels call Repository methods (suspend functions)
- Repositories call TmdbApiService and map DTOs to domain models
- Never call ApiService directly from a ViewModel

## Adding a New Feature
1. Create `feature/<name>/` directory with `<Name>Screen.kt` + `<Name>ViewModel.kt`
2. Define a `<Name>UiState` data class in the ViewModel file
3. Register ViewModel in `core/di/ViewModelModule.kt`
4. Add `@Serializable` route to `core/navigation/AppRoutes.kt`
5. Add `composable<AppRoutes.NewRoute>` in `core/navigation/AppNavGraph.kt`
6. Write tests in `commonTest/.../feature/<Name>ViewModelTest.kt`

## Adding a New API Endpoint
1. Add the Ktor call in `core/data/remote/TmdbApiService.kt`
2. Add `@Serializable` DTO in `core/data/model/dto/`
3. Add mapper extension function in `core/data/mapper/MovieMapper.kt`
4. Expose through `MovieRepository` interface + `TmdbMovieRepository` implementation

## Adding a Reusable Component
1. Create in `core/ui/components/<ComponentName>.kt`
2. Use `AppColors`, `Dimensions`, `AppTypography` for theming - no hardcoded values
3. Accept a `Modifier` parameter and `onClick` lambda where applicable
4. Add shimmer placeholder variant if the component displays async data

## Navigation
- Routes: `@Serializable` sealed interface members in `AppRoutes.kt`
- Complex args (lists): JSON-encode as String in route params
- Bottom nav destinations use crossfade transitions
- Forward navigation uses slide transitions (defined in `NavTransitions.kt`)
- Use `navigateToDestination()` helper for bottom nav to preserve back stack

## Dependency Injection
- Modules: `NetworkModule` (HttpClient, ApiService), `RepositoryModule`, `ViewModelModule`
- `WatchListRepository` is a Koin `single` (shared across ViewModels)
- `MovieRepository` is a Koin `singleOf` bound to `TmdbMovieRepository`
- ViewModels use `koinViewModel()` in Compose, or `viewModelOf()` in module
- Android inits Koin in `MainActivity.onCreate()`, iOS in `MainViewController.kt`
