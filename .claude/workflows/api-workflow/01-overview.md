# API Creation Workflow — Overview

## What you will create
Adding a new API endpoint touches these layers:

```
1. DTO                  → @Serializable data class matching JSON response
2. Domain model         → Clean data class for app/UI use
3. Mapper               → Extension function: DTO → Domain
4. ApiService method    → Ktor HTTP call returning DTO
5. Repository interface → Add suspend function to interface
6. Repository impl      → Call ApiService + map to domain model
7. Koin DI              → Register new services (if any)
8. Test fake            → Add method to FakeMovieRepository
9. Mapper test          → Test DTO-to-domain mapping
```

## File locations
```
composeApp/src/commonMain/kotlin/com/example/themoviedb/
├── core/data/
│   ├── model/
│   │   ├── dto/<Name>Dto.kt           ← Step 1
│   │   └── <Name>Item.kt              ← Step 2
│   ├── mapper/MovieMapper.kt          ← Step 3 (add to existing file)
│   ├── remote/
│   │   ├── TmdbApiService.kt          ← Step 4 (add method)
│   │   ├── ApiConstants.kt            ← Base URLs and auth
│   │   └── HttpClientFactory.kt       ← Platform HTTP engines
│   └── repository/
│       ├── MovieRepository.kt         ← Step 5 (add to interface)
│       └── TmdbMovieRepository.kt     ← Step 6 (add implementation)
├── core/di/
│   ├── NetworkModule.kt               ← Step 7 (HttpClient, ApiService)
│   ├── RepositoryModule.kt            ← Step 7 (repositories)
│   └── AppModules.kt                  ← Step 7 (module list)
composeApp/src/commonTest/
├── fake/FakeMovieRepository.kt         ← Step 8
└── core/data/MovieMapperTest.kt        ← Step 9
```

## Data flow
```
TMDB REST API
    ↓  HTTP GET (Ktor HttpClient)
TmdbApiService              returns DTO (@Serializable)
    ↓
TmdbMovieRepository         maps DTO → Domain model, handles errors
    ↓
ViewModel                   calls repository, updates StateFlow
    ↓
Screen                      collects StateFlow, renders UI
```

## Dependency Injection flow
```
NetworkModule provides:
    Json → HttpClient → TmdbApiService

RepositoryModule provides:
    TmdbMovieRepository (uses TmdbApiService) → binds to MovieRepository interface

ViewModelModule provides:
    ViewModels (use MovieRepository interface via get())
```

## Detailed steps (separate files)
1. [02-dto.md](./02-dto.md) — Create the DTO
2. [03-domain-model.md](./03-domain-model.md) — Create the domain model
3. [04-mapper.md](./04-mapper.md) — Write the mapper
4. [05-api-service.md](./05-api-service.md) — Add API call to TmdbApiService
5. [06-repository.md](./06-repository.md) — Add to repository (interface + impl)
6. [07-dependency-injection.md](./07-dependency-injection.md) — DI best practices
7. [08-test-fake.md](./08-test-fake.md) — Update test fake
8. [09-mapper-test.md](./09-mapper-test.md) — Write mapper tests
9. [10-verify.md](./10-verify.md) — Build and verify
