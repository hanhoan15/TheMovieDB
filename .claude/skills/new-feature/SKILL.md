---
name: new-feature
description: Scaffold a new feature module with Screen, ViewModel, UiState, route, DI registration, and test
user-invocable: true
argument-hint: "<feature-name>"
---

# Scaffold New Feature

Create a new feature module for `$ARGUMENTS` following project conventions.

## Files to Create

1. **Screen**: `composeApp/src/commonMain/kotlin/com/example/themoviedb/feature/$0/$0Screen.kt`
   - Composable function with `Scaffold`, `AppColors.ScreenBackground`
   - Accept `onBack: () -> Unit` and any navigation callbacks
   - Use `koinViewModel()` to obtain ViewModel
   - Observe `viewModel.uiState.collectAsState()`

2. **ViewModel**: `composeApp/src/commonMain/kotlin/com/example/themoviedb/feature/$0/$0ViewModel.kt`
   - Extend `ViewModel()`
   - Define `data class ${0}UiState(val isLoading: Boolean = true)`
   - Use `MutableStateFlow` + `StateFlow` pattern
   - Inject `MovieRepository` via constructor

3. **Route**: Add `@Serializable data object` to `AppRoutes.kt`

4. **Navigation**: Add `composable<AppRoutes.$0>` block in `AppNavGraph.kt`

5. **DI**: Register ViewModel in `ViewModelModule.kt` using `viewModelOf(::${0}ViewModel)`

6. **Test**: `composeApp/src/commonTest/kotlin/com/example/themoviedb/feature/${0}ViewModelTest.kt`
   - Use `StandardTestDispatcher`, `runTest`, `advanceUntilIdle`
   - Test initial loading state and loaded state

## Conventions
- Use PascalCase for the feature name in class names
- Use lowercase for directory name
- Follow existing patterns from HomeScreen/HomeViewModel as reference
