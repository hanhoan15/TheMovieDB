---
name: create-screen
description: Step-by-step flow to create a new Compose screen with ViewModel, UiState, DI, navigation, and test
user-invocable: true
argument-hint: "<FeatureName>"
---

# Flow: Create a New Screen

Create a complete screen for feature **$ARGUMENTS** following project conventions.
Use PascalCase for the feature name in classes, lowercase for directory.

---

## Step 1: Create the UiState + ViewModel

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/feature/<lowercase>/
${0}ViewModel.kt`

```kotlin
package com.example.themoviedb.feature.<lowercase>

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.themoviedb.core.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ${0}UiState(
    val isLoading: Boolean = true,
    // Add fields for this screen's data
)

class ${0}ViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(${0}UiState())
    val uiState: StateFlow<${0}UiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            // Call repository, then update state:
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
```

**Key rules:**
- Always use `MutableStateFlow` + `StateFlow` (never `mutableStateOf`)
- Always expose `uiState` as read-only `StateFlow` via `.asStateFlow()`
- Use `_uiState.update { it.copy(...) }` for thread-safe state updates
- Launch coroutines in `viewModelScope`
- Inject dependencies via constructor (Koin will provide them)
- If you need a route parameter (e.g., movieId), add it as a constructor param:
  ```kotlin
  class ${0}ViewModel(
      private val someId: Int,
      private val repository: MovieRepository,
  ) : ViewModel()
  ```

---

## Step 2: Create the Screen composable

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/feature/<lowercase>/
${0}Screen.kt`

```kotlin
package com.example.themoviedb.feature.<lowercase>

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.ui.components.AppBottomNavBar
import com.example.themoviedb.core.ui.components.BottomDestination
import com.example.themoviedb.core.ui.theme.AppColors
import com.example.themoviedb.core.ui.theme.AppTypography
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ${0}Screen(
    onMovieClick: (MovieItem) -> Unit,          // navigation callbacks first
    onBack: () -> Unit,
    onDestinationSelected: (BottomDestination) -> Unit,
    viewModel: ${0}ViewModel = koinViewModel(), // ViewModel last with default
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppColors.ScreenBackground,
        bottomBar = {
            AppBottomNavBar(
                selected = BottomDestination.HOME,  // change as needed
                onSelect = onDestinationSelected,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.ScreenBackground)
                .padding(innerPadding)
                .padding(horizontal = 25.dp, vertical = 20.dp),
        ) {
            // Top bar row with back button + centered title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.DetailBackButton,
                    modifier = Modifier.size(22.dp).clickable(onClick = onBack),
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$ARGUMENTS",
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = AppTypography.montserratSemiBold(),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        ),
                    )
                }
                Spacer(modifier = Modifier.size(22.dp)) // balance the back icon
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Screen content goes here
            // Use uiState.isLoading to show shimmer placeholders
            // Use uiState.data to display content
        }
    }
}
```

**Key rules:**
- ViewModel injected via `koinViewModel()` as default parameter (last param)
- Collect state with `val uiState by viewModel.uiState.collectAsState()`
- Use `Scaffold` with `containerColor = AppColors.ScreenBackground`
- Apply `innerPadding` from Scaffold to avoid bottom bar overlap
- Navigation callbacks are lambdas, not interfaces
- Use `AppColors`, `AppTypography`, `Dimensions` — never hardcode colors/sizes
- For loading states, use `ShimmerPlaceholder` from `core/ui/components/`

---

## Step 3: Register ViewModel in Koin

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/di/ViewModelModule.kt`

Add to the `viewModelModule` val:

```kotlin
// Without route parameters:
viewModelOf(::${0}ViewModel)

// With route parameters (e.g., an ID from the route):
viewModel { params -> ${0}ViewModel(params.get(), get()) }
```

**Key rules:**
- `viewModelOf(::ClassName)` for simple ViewModels (Koin auto-resolves constructor params)
- `viewModel { params -> }` when you need to pass navigation arguments via `params.get()`
- All repository/service deps are resolved automatically by Koin via `get()`

---

## Step 4: Add navigation route

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/navigation/AppRoutes.kt`

Add inside the `sealed interface AppRoutes`:

```kotlin
// Simple route (no params):
@Serializable
data object ${0} : AppRoutes

// Route with parameters:
@Serializable
data class ${0}(val someId: Int) : AppRoutes
```

**Key rules:**
- All routes must be `@Serializable` (kotlinx.serialization)
- Use `data object` for no-param routes, `data class` for parameterized routes
- Only use primitive types or String as route params
- For complex data (lists), JSON-encode as String:
  ```kotlin
  @Serializable
  data class ${0}(val itemsJson: String, val initialIndex: Int) : AppRoutes
  ```

---

## Step 5: Wire up in NavGraph

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/navigation/AppNavGraph.kt`

Add inside the `NavHost` block:

```kotlin
// Simple route:
composable<AppRoutes.${0}> {
    ${0}Screen(
        onMovieClick = { movie ->
            navController.navigate(AppRoutes.Detail(movieId = movie.id))
        },
        onBack = { navController.popBackStack() },
        onDestinationSelected = { dest ->
            navigateToDestination(navController, dest)
        },
    )
}

// Route with parameters:
composable<AppRoutes.${0}> { backStackEntry ->
    val route = backStackEntry.toRoute<AppRoutes.${0}>()
    ${0}Screen(
        someId = route.someId,
        onBack = { navController.popBackStack() },
        onDestinationSelected = { dest ->
            navigateToDestination(navController, dest)
        },
    )
}
```

**Key rules:**
- Use `composable<AppRoutes.RouteName>` (type-safe, no string routes)
- Extract params with `backStackEntry.toRoute<AppRoutes.RouteName>()`
- Use `navigateToDestination()` helper for bottom nav tabs
- For custom transitions, add `enterTransition`/`exitTransition` params:
  ```kotlin
  composable<AppRoutes.${0}>(
      enterTransition = { NavTransitions.crossfadeIn() },
      exitTransition = { NavTransitions.crossfadeOut() },
  ) { ... }
  ```

---

## Step 6: Create test

**File:** `composeApp/src/commonTest/kotlin/com/example/themoviedb/feature/${0}ViewModelTest.kt`

```kotlin
package com.example.themoviedb.feature

import com.example.themoviedb.fake.FakeMovieRepository
import com.example.themoviedb.feature.<lowercase>.${0}ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class ${0}ViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoadCompletes() = runTest {
        val repository = FakeMovieRepository()
        val viewModel = ${0}ViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
    }
}
```

**Key rules:**
- Always `Dispatchers.setMain(StandardTestDispatcher())` in `@BeforeTest`
- Always `Dispatchers.resetMain()` in `@AfterTest`
- Always `advanceUntilIdle()` after creating ViewModel (init coroutines need to run)
- Use `FakeMovieRepository()` — configure via `.moviesByCategory` and `.detailById` maps
- Assert on `viewModel.uiState.value` fields
- Run: `./gradlew composeApp:testDebugUnitTest --tests "*.${0}ViewModelTest"`

---

## Step 7: Verify

```bash
./gradlew composeApp:compileDebugKotlinAndroid composeApp:compileKotlinIosSimulatorArm64
./gradlew composeApp:testDebugUnitTest --tests "*.${0}ViewModelTest"
```
