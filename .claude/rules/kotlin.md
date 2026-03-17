---
paths:
  - "composeApp/**/*.kt"
---

# Kotlin & Compose Conventions

## Code Style
- Use trailing commas in function parameters, constructor args, and collection literals
- Prefer `data class` for models, `sealed interface` for ADTs
- Use `@Serializable` (kotlinx) for all DTOs and navigation routes - never Moshi or Gson
- Use extension functions for mapping (e.g., `fun Dto.toDomain()`)
- Prefer `object` for singletons (colors, dimensions, constants)

## Compose
- Composable functions are PascalCase, non-composable helpers are camelCase
- Pass callbacks as lambda parameters (`onClick: () -> Unit`), not interfaces
- Use `Modifier` as first optional parameter in reusable composables
- Prefer `MaterialTheme.typography` / `MaterialTheme.colorScheme` over hardcoded values
- Use `AppColors`, `AppTypography`, `Dimensions` from `core/ui/theme/` for project-specific tokens
- Never use `mutableStateOf` in ViewModels - use `MutableStateFlow` instead

## Coroutines
- Launch coroutines in `viewModelScope` inside ViewModels
- Use `StateFlow` (not `SharedFlow`) for UI state
- Use `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)` for derived flows
- Cancel jobs explicitly when switching contexts (e.g., tab change cancels previous fetch)

## Multiplatform
- All shared code in `commonMain` - platform code only for `expect`/`actual`
- Never import `android.*` or `platform.darwin.*` in commonMain
- Use Ktor `HttpClient` (not Retrofit or URLSession) for networking
- Use `compose.components.resources` for shared resources (images, fonts, strings)
