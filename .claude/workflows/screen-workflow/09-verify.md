# Step 8: Build and Verify

## Checklist before building

- [ ] UiState data class defined with sensible defaults
- [ ] ViewModel created with `MutableStateFlow` + `StateFlow`
- [ ] Screen composable collects state with `collectAsState()`
- [ ] `@Serializable` route added to `AppRoutes.kt`
- [ ] `composable<AppRoutes.YourRoute>` added to `AppNavGraph.kt`
- [ ] ViewModel registered in `ViewModelModule.kt`
- [ ] Import added for ViewModel in `ViewModelModule.kt`
- [ ] Test created with `@BeforeTest`/`@AfterTest` dispatcher setup

## Build commands

```bash
# Compile both platforms:
./gradlew composeApp:compileDebugKotlinAndroid composeApp:compileKotlinIosSimulatorArm64

# Run tests:
./gradlew composeApp:testDebugUnitTest

# Full verification (both builds + tests):
./gradlew composeApp:compileDebugKotlinAndroid composeApp:compileKotlinIosSimulatorArm64 composeApp:testDebugUnitTest
```

## Common errors and fixes

| Error | Cause | Fix |
|-------|-------|-----|
| `Unresolved reference: koinViewModel` | Missing import | Add `import org.koin.compose.viewmodel.koinViewModel` |
| `Unresolved reference: <ViewModel>` | Missing import in ViewModelModule | Add import for your ViewModel class |
| `No definition found for class '<ViewModel>'` | Not registered in Koin | Add `viewModel { ... }` in ViewModelModule |
| `@Serializable not applicable` | Missing serialization plugin | Verify `kotlinSerialization` plugin in build.gradle.kts |
| `No value passed for parameter` | Route param not provided | Check `navController.navigate(AppRoutes.YourRoute(param = value))` |
| `Type mismatch: inferred type is Unit` | Missing return in composable lambda | Check `composable<>` block has correct screen call |
