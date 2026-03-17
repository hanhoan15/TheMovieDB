# Step 8: Write Mapper Tests

**File:** `composeApp/src/commonTest/kotlin/com/example/themoviedb/core/data/MovieMapperTest.kt`

Add tests to the existing mapper test file.

## Template

```kotlin
@Test
fun newDtoMapper_happyPath() {
    val dto = NewThingDto(
        id = 1,
        name = "Test Name",
        posterPath = "/poster.jpg",
        voteAverage = 7.5,
    )
    val item = dto.toNewThingItem("https://image.tmdb.org/t/p/w500")

    assertEquals(1, item.id)
    assertEquals("Test Name", item.name)
    assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", item.imageUrl)
    assertEquals(7.5, item.rating)
}

@Test
fun newDtoMapper_handlesNulls() {
    val dto = NewThingDto(id = 0)   // all other fields use defaults
    val item = dto.toNewThingItem("https://image.tmdb.org/t/p/w500")

    assertEquals(0, item.id)
    assertEquals("", item.name)
    assertNull(item.imageUrl)       // no poster path → null URL
}

@Test
fun newDtoMapper_returnsNullForMissingImage() {
    val dto = NewThingDto(id = 1, posterPath = null)
    val item = dto.toNewThingItem("https://image.tmdb.org/t/p/w500")

    assertNull(item)    // if mapper returns null for missing poster
}
```

## What to test

### Always test
1. **Happy path** — all fields populated correctly
2. **Null handling** — nullable DTO fields produce safe defaults
3. **Image URL construction** — base URL + path = full URL
4. **Missing critical data** — returns null when required data absent

### Test each utility function
```kotlin
@Test fun buildImageUrl_validPath() {
    assertEquals("https://base/path.jpg", buildImageUrl("https://base", "/path.jpg"))
}

@Test fun buildImageUrl_nullPath() {
    assertNull(buildImageUrl("https://base", null))
}

@Test fun buildImageUrl_blankPath() {
    assertNull(buildImageUrl("https://base", ""))
}
```

## Mapper test rules
- No coroutine setup needed — mappers are pure functions
- No `@BeforeTest`/`@AfterTest` required
- Test edge cases: null, empty string, blank string, zero values
- Test image URL construction separately from main mapping
- Keep in existing `MovieMapperTest.kt` file

## Running
```bash
./gradlew composeApp:testDebugUnitTest --tests "*.MovieMapperTest"
```

---

## Why these choices?

### Why test mappers separately (not through repository)?
- Mappers are pure functions — no coroutines, no mocking, no setup/teardown
- Tests run instantly (no `advanceUntilIdle()`, no dispatchers)
- Catch mapping bugs early — before they propagate through the data layer
- Edge cases (null paths, empty strings) are easier to test in isolation

### Why no `@BeforeTest` / `@AfterTest`?
- Mapper tests don't use `Dispatchers.Main` — no `setMain`/`resetMain` needed
- No shared mutable state between tests — each test creates its own DTO
- No cleanup needed — pure function tests are inherently isolated

### Why test null handling specifically?
```kotlin
@Test fun newDtoMapper_handlesNulls() {
    val dto = NewThingDto(id = 0)   // all defaults
    val item = dto.toNewThingItem("https://base")
    assertNull(item)  // or check safe defaults
}
```
TMDB API frequently returns `null` for optional fields (missing posters, no backdrop, etc.). Testing null handling prevents:
- `NullPointerException` in production when a movie has no poster
- Broken image URLs like `"https://base/null"` instead of actual null
- Crash on `title!!` if the API ever sends null for title
