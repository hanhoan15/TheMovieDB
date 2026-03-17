# Step 9: Build and Verify

## Checklist

- [ ] DTO created with `@Serializable` and `@SerialName` for snake_case fields
- [ ] Domain model created (no `@Serializable`)
- [ ] Mapper extension function added to `MovieMapper.kt`
- [ ] API method added to `TmdbApiService.kt`
- [ ] Method added to `MovieRepository` interface
- [ ] Method implemented in `TmdbMovieRepository` with error handling
- [ ] New services registered in Koin (if any)
- [ ] `FakeMovieRepository` updated with new method
- [ ] Mapper test added to `MovieMapperTest.kt`

## Build commands

```bash
# Compile both platforms:
./gradlew composeApp:compileDebugKotlinAndroid composeApp:compileKotlinIosSimulatorArm64

# Run mapper tests:
./gradlew composeApp:testDebugUnitTest --tests "*.MovieMapperTest"

# Run all tests:
./gradlew composeApp:testDebugUnitTest

# Full check:
./gradlew composeApp:compileDebugKotlinAndroid composeApp:compileKotlinIosSimulatorArm64 composeApp:testDebugUnitTest
```

## Common errors

| Error | Cause | Fix |
|-------|-------|-----|
| `Serializer has not been found for type 'X'` | Missing `@Serializable` on DTO | Add `@Serializable` annotation |
| `Expected property name` at runtime | `@SerialName` doesn't match JSON key | Check actual JSON key name from API |
| `No definition found for class 'X'` | New service not in Koin | Add `single { }` in appropriate module |
| `Unresolved reference: toNewThingItem` | Missing import in Repository | Add import for mapper function |
| `Class 'FakeMovieRepository' is not abstract and does not implement method` | Forgot to add to fake | Add override to `FakeMovieRepository` |
| `NullPointerException` at runtime | DTO field non-nullable but API returns null | Make field nullable with `= null` default |

## Testing the API manually

To verify the TMDB endpoint returns expected data:
```bash
curl -s "https://api.themoviedb.org/3/movie/550?api_key=394cba9acc6f443b1abfc75085b89adc" | python3 -m json.tool | head -30
```
Replace the path (`movie/550`) with your endpoint to inspect the JSON structure.
