---
name: add-component
description: Create a new reusable Compose component in core/ui/components
user-invocable: true
argument-hint: "<ComponentName>"
---

# Add Reusable Component

Create a new reusable Compose component: **$ARGUMENTS**

## Steps

1. Create file at `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/ui/components/$ARGUMENTS.kt`

2. Follow this structure:
   ```kotlin
   @Composable
   fun $ARGUMENTS(
       // required params first
       modifier: Modifier = Modifier,
       // optional params with defaults
   ) {
       // Implementation using AppColors, Dimensions, AppTypography
   }
   ```

3. Conventions:
   - Use `AppColors` for colors, `Dimensions` for sizes, `AppTypography` for fonts
   - Accept `Modifier` as a parameter with `Modifier` default
   - Accept click handlers as `() -> Unit` lambdas
   - Use `Surface` with `RoundedCornerShape` for cards
   - Use `AsyncImageWithPlaceholder` for remote images
   - If the component loads async data, also create a shimmer placeholder variant:
     ```kotlin
     @Composable
     fun ${ARGUMENTS}Placeholder(modifier: Modifier = Modifier) { ... }
     ```

4. Verify it compiles: `./gradlew composeApp:compileDebugKotlinAndroid`
