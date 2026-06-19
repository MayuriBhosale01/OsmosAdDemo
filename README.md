# Osmos Ad Demo — Android App

## Setup Steps
1. Clone the repo: `git clone <your-repo-url>`
2. Open in Android Studio
3. Let Gradle sync complete
4. Run on emulator or physical device (API 21+)

## Architecture
MVVM + Clean Architecture
- `AdViewModel` → StateFlow for UI state
- `AdRepository` → handles ad fetching with retry
- `VisibilityTracker` → reusable 50% visibility helper
- `AdLogger` → centralized logging to Logcat + UI

## How Ad Fetching Works
- Tap "Load Ad" → `AdViewModel.loadAd()` called
- `AdRepository.fetchBannerAd()` calls Osmos SDK with:
  - cliUbid = "Any"
  - pageType = "demo_page"
  - adUnit = "banner_ads"
- Response parsed: `ads.banner_ads[0]`
- Retry mechanism: 3 retries with exponential backoff

## How Impression Logic Works (50% Visibility)
- `VisibilityTracker` attaches to banner ImageView
- Uses `getGlobalVisibleRect(rect)` on every scroll/layout change
- If `visibleHeight / totalHeight >= 0.5` → impression fired
- Fires only ONCE per ad load (boolean flag guard)

## How Click Tracking Works
- User taps ImageView
- `destination_url` opens in browser via `Intent.ACTION_VIEW`
- `OsmosSDK.registerClickEvent()` called on background thread
- Both happen simultaneously — tracking failure won't block navigation

## Error Handling
- SDK init failure → logged, app continues
- No ads in response → shows "Ad not available"
- Network error → shows error message, retry attempted
- Invalid fields → graceful fallback UI
- No crashes in any scenario

## How to Run
1. Build & run on Android emulator or device
2. Tap "Load Ad"
3. Banner loads → impression fires automatically
4. Tap banner → browser opens, click event fires
5. Check Logcat tag: `OsmosAd` for all events

## Assumptions
- SDK initialization is synchronous in Application.onCreate()
- ads.banner_ads[0] is the primary ad unit
- Impression valid at >= 50% viewport visibility

## Challenges
- Detecting partial visibility accurately across scroll events
- Ensuring impression fires exactly once
- Handling all SDK error states without crashes
