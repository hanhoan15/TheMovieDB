---
name: create-repository
description: Step-by-step flow to create a new Repository with interface, implementation, DI registration, and test fake
user-invocable: true
argument-hint: "<RepositoryName>"
---

# Flow: Create a New Repository

Create a new repository: **$ARGUMENTS**

---

## Step 1: Define the interface

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/repository/${0}Repository.kt`

```kotlin
package com.example.themoviedb.core.data.repository

interface ${0}Repository {
    suspend fun getItems(): List<ItemType>
    suspend fun getById(id: Int): ItemType?
    // Define all data access methods as suspend functions
}
```

**Key rules:**
- Interface only — no implementation details
- All methods are `suspend` (called from coroutines)
- Return nullable types for single-item lookups (null = not found)
- Return empty collections for list lookups on failure (not null)

---

## Step 2: Create the implementation

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/repository/${0}RepositoryImpl.kt`

### Option A: API-backed repository (like TmdbMovieRepository)

```kotlin
package com.example.themoviedb.core.data.repository

import com.example.themoviedb.core.data.remote.TmdbApiService

class ${0}RepositoryImpl(
    private val apiService: TmdbApiService,
) : ${0}Repository {

    override suspend fun getItems(): List<ItemType> {
        return try {
            val response = apiService.getSomething()
            response.results.map { it.toDomainItem() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getById(id: Int): ItemType? {
        return try {
            val response = apiService.getSomethingById(id)
            response.toDomainItem()
        } catch (e: Exception) {
            null
        }
    }
}
```

### Option B: In-memory repository with StateFlow (like WatchListRepository)

```kotlin
package com.example.themoviedb.core.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ${0}Repository {
    private val _items = MutableStateFlow<List<ItemType>>(emptyList())
    val items: StateFlow<List<ItemType>> = _items.asStateFlow()

    fun add(item: ItemType) {
        _items.value = _items.value + item
    }

    fun remove(id: Int) {
        _items.value = _items.value.filter { it.id != id }
    }

    fun toggle(item: ItemType) {
        val current = _items.value
        _items.value = if (current.any { it.id == item.id }) {
            current.filter { it.id != item.id }
        } else {
            current + item
        }
    }

    fun isPresent(id: Int): Boolean = _items.value.any { it.id == id }
}
```

**Key rules:**
- API repositories: wrap all API calls in try/catch, return safe defaults
- In-memory repositories: use `MutableStateFlow` for reactive state
- Map DTOs to domain models inside the repository (not in ViewModel)
- Constructor-inject `TmdbApiService` (Koin will provide it)

---

## Step 3: Register in Koin

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/di/RepositoryModule.kt`

```kotlin
val repositoryModule = module {
    // existing...

    // API-backed (interface + implementation):
    singleOf(::${0}RepositoryImpl).bind<${0}Repository>()

    // In-memory singleton (no interface):
    single { ${0}Repository() }
}
```

**Key rules:**
- Use `single` (not `factory`) — repositories hold state/caches
- `singleOf(::Impl).bind<Interface>()` when you have an interface
- `single { Class() }` for standalone singletons

---

## Step 4: Create test fake

**File:** `composeApp/src/commonTest/kotlin/com/example/themoviedb/fake/Fake${0}Repository.kt`

```kotlin
package com.example.themoviedb.fake

import com.example.themoviedb.core.data.repository.${0}Repository

class Fake${0}Repository : ${0}Repository {

    var itemsResult: List<ItemType> = emptyList()
    var byIdResults: MutableMap<Int, ItemType?> = mutableMapOf()

    override suspend fun getItems(): List<ItemType> = itemsResult

    override suspend fun getById(id: Int): ItemType? = byIdResults[id]
}
```

**Key rules:**
- Fakes are simple in-memory implementations with configurable results
- Use mutable properties so tests can set up expected return values
- No mocking framework needed — manual fakes are simpler and more readable
- For in-memory repositories (like WatchListRepository), just instantiate the real class in tests

**Existing fake pattern (FakeMovieRepository):**
```kotlin
class FakeMovieRepository : MovieRepository {
    val moviesByCategory = mutableMapOf<MovieCategory, List<MovieItem>>()
    val detailById = mutableMapOf<Int, MovieDetailItem?>()
    var searchResults: List<MovieItem> = emptyList()

    override suspend fun getMovies(category: MovieCategory, page: Int) =
        moviesByCategory[category].orEmpty()
    override suspend fun getMovieDetail(movieId: Int) = detailById[movieId]
    override suspend fun searchMovies(query: String, page: Int) = searchResults
}
```

---

## Step 5: Verify

```bash
./gradlew composeApp:compileDebugKotlinAndroid composeApp:compileKotlinIosSimulatorArm64
./gradlew composeApp:testDebugUnitTest
```
