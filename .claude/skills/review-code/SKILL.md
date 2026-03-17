---
name: review-code
description: Review code changes for issues, style violations, and improvements
user-invocable: true
allowed-tools: Bash(git *), Read, Grep, Glob
argument-hint: "[file path or blank for all staged changes]"
---

# Code Review

Review code for quality, correctness, and adherence to project conventions.

## What to Review

If `$ARGUMENTS` is a file path, review that file. Otherwise review all staged/unstaged changes:
```
git diff
git diff --cached
```

## Checklist

### Correctness
- [ ] No potential crashes (null safety, index bounds)
- [ ] Coroutines properly scoped to `viewModelScope`
- [ ] StateFlow used correctly (not collecting in wrong scope)
- [ ] Navigation routes are `@Serializable`

### Architecture
- [ ] Follows MVVM: Screen -> ViewModel -> Repository -> ApiService
- [ ] No direct API calls from ViewModel (use Repository)
- [ ] New ViewModels registered in `ViewModelModule.kt`
- [ ] Domain models separated from DTOs

### Kotlin/Compose
- [ ] Trailing commas used
- [ ] No hardcoded colors/dimensions (use AppColors, Dimensions)
- [ ] Composables accept `Modifier` parameter where appropriate
- [ ] No `android.*` imports in `commonMain`

### Multiplatform
- [ ] New code is in `commonMain` unless platform-specific
- [ ] `expect`/`actual` used for platform differences
- [ ] Builds on both Android and iOS

## Output
Provide findings grouped by severity: Blocking, Warning, Suggestion.
