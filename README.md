# Trust Signals SDK for Android

Collect and upload device trust signals — location, network state, Bluetooth, Wi-Fi, telephony, battery, and more — from Android apps to the Futurae Trust Signals backend.

**[API Reference (Dokka)](https://futurae-technologies.github.io/android-trust-signals-sdk/)** &nbsp;|&nbsp;
**[Changelog](https://github.com/Futurae-Technologies/android-trust-signals-sdk/releases)**

---

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Permissions](#permissions)
  - [Background location for scheduled collections](#background-location-for-scheduled-collections)
- [Core Types](#core-types)
- [Getting Started](#getting-started)
  - [1. Initialize the SDK](#1-initialize-the-sdk)
  - [2. Collect and Upload](#2-collect-and-upload)
  - [3. Schedule Periodic Collections](#3-schedule-periodic-collections)
  - [4. Handle Errors and Stop or Reschedule](#4-handle-errors-and-stop-or-reschedule)
- [Sample App](#sample-app)
- [Releases](#releases)

---

## Requirements

| Requirement        | Minimum                  |
|--------------------|--------------------------|
| Android API level  | 23 (Android 6.0)         |
| Kotlin             | 1.9+                     |
| compileSdk         | 36                       |

---

## Installation

The SDK is distributed via [GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry).

### 1. Authenticate with GitHub Packages

Add your GitHub credentials to `gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

Your token needs the `read:packages` scope.

### 2. Add the Maven repository

In your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/Futurae-Technologies/android-trust-signals-sdk")
      credentials {
        username = providers.gradleProperty("gpr.user").orNull
        password = providers.gradleProperty("gpr.key").orNull
      }
    }
  }
}
```

### 3. Add the dependency

In your app or module `build.gradle.kts`:

```kotlin
dependencies {
  implementation("com.futurae.sdk:trust-signals:<version>")
}
```

Replace `<version>` with the [latest release tag](https://github.com/Futurae-Technologies/android-trust-signals-sdk/releases).

---

## Permissions

The SDK declares the following permissions in its AAR manifest, which are merged into your app automatically:

| Permission | Level | Notes |
|---|---|---|
| `ACCESS_FINE_LOCATION` | Runtime | Required for location and Wi-Fi/BLE scans |
| `ACCESS_COARSE_LOCATION` | Runtime | Fallback coarse location |
| `BLUETOOTH_SCAN` | Runtime | BLE scan on API 31+ |
| `BLUETOOTH_CONNECT` | Runtime | BLE connect on API 31+ |
| `BLUETOOTH` / `BLUETOOTH_ADMIN` | Normal | BLE on API ≤ 30 (declared with `maxSdkVersion="30"`) |
| `NEARBY_WIFI_DEVICES` | Runtime | Wi-Fi scan on API 33+ |
| `READ_PHONE_STATE` | Runtime | Telephony signal collection |
| `INTERNET` | Normal | Upload to backend |
| `ACCESS_WIFI_STATE` / `CHANGE_WIFI_STATE` | Normal | Wi-Fi state collection |
| `ACCESS_NETWORK_STATE` / `CHANGE_NETWORK_STATE` | Normal | Network state collection |
| `FOREGROUND_SERVICE` | Normal | Location foreground service |
| `FOREGROUND_SERVICE_LOCATION` | Normal | Required on API 34+ for location foreground service type |

Your app is responsible for requesting runtime permissions before calling SDK methods. Signals that require a missing permission are gracefully skipped rather than throwing.

### Background location for scheduled collections

`ACCESS_BACKGROUND_LOCATION` is **not** declared in the SDK manifest because inheriting it automatically would subject every host app to Google Play's background location review process, regardless of whether they use scheduled collections.

If your app intends to use `scheduleCollections()` to collect when the app is in the background, declare and request this permission yourself to enable location collection during background jobs:

**`AndroidManifest.xml`:**
```xml
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
```

**Request at runtime (API 29+):**
```kotlin
// Must be requested separately, after ACCESS_FINE_LOCATION is already granted.
// Android requires a separate prompt for background location.
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
  ActivityCompat.requestPermissions(
    activity,
    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
    REQUEST_CODE_BACKGROUND_LOCATION,
  )
}
```

> Without this permission on API 29+, scheduled collections will run normally but the `locationCollection` field in each `TSCollection` will be `null`.

---

## Core Types

All public types are in the `com.futurae.sdk.ts.model.public` package. The main entry point `TrustSignalsSDK` and the error types `TSAuthenticationException` / `TSUploadException` live in `com.futurae.sdk.ts.error`.

### `TSConfiguration`

Holds the static SDK configuration. Passed once to `TrustSignalsSDK.initialize()`.

```kotlin
import com.futurae.sdk.ts.model.public.TSConfiguration

TSConfiguration(
  serverURL = "https://your-trust-signals-server.example.com",
  collectionTimeoutMS = 20_000L, // optional, default is 20 000 ms
)
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `serverURL` | `String` | Yes | Base URL of the Trust Signals backend that will receive uploaded observations. |
| `collectionTimeoutMS` | `Long` | No | Maximum time in milliseconds to wait for all signal collectors before returning a partial result. Defaults to 20 000 ms. |

---

### `TSCollectionRequest`

Carries the per-account credentials used to authenticate an upload. Passed to `collectAndUpload()` and `scheduleCollections()`. Multiple requests can be supplied in a single call to upload for several accounts in parallel.

```kotlin
import com.futurae.sdk.ts.model.public.TSCollectionRequest

TSCollectionRequest(
  accountId = "user-account-id",
  accessToken = "bearer-token",
  appId = "your-app-id",
)
```

| Parameter | Type | Description                                                                                                                                                                  |
|---|---|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `accountId` | `String` | Identifies the end-user account associated with this upload.                                                                                                                 |
| `accessToken` | `String` | Bearer token used to authenticate the upload request. An expired or invalid token is reported as `TSAuthenticationException` (HTTP 401/403) in `TSUploadException.failures`. |
| `appId` | `String` | Identifier for host app and tenant.                                                                                                                                          |

---

## Getting Started

### 1. Initialize the SDK

Call `initialize` once, as early as possible — typically in your `Application.onCreate()`:

```kotlin
import com.futurae.sdk.ts.TrustSignalsSDK
import com.futurae.sdk.ts.model.public.TSConfiguration

class MyApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    TrustSignalsSDK.initialize(
      context = this,
      configuration = TSConfiguration(
        serverURL = "https://your-trust-signals-server.example.com",
      )
    )
  }
}
```

See [`TSConfiguration`](#tsconfiguration) for a full parameter reference.

---

### 2. Collect and Upload

#### Using coroutines (recommended)

```kotlin
import com.futurae.sdk.ts.TrustSignalsSDK
import com.futurae.sdk.ts.model.public.TSCollectionRequest
import com.futurae.sdk.ts.error.TSAuthenticationException
import com.futurae.sdk.ts.error.TSUploadException

// Collect only (no upload)
val collection = TrustSignalsSDK.collect()

// Collect and upload — single account
try {
  val collection = TrustSignalsSDK.collectAndUpload(
    TSCollectionRequest(
      accountId = "user-account-id",
      accessToken = "bearer-token",
      appId = "your-app-id",
    )
  )
  // collection contains the signals that were uploaded
} catch (e: TSUploadException) {
  // One or more uploads failed — inspect per-account causes
  e.failures.forEach { (accountId, error) ->
    if (error is TSAuthenticationException) { /* refresh token for accountId */ }
  }
} catch (e: IllegalStateException) {
  // SDK not initialized
}
```

#### Multiple accounts — signals collected once, uploaded in parallel

`collectAndUpload` accepts any number of `TSCollectionRequest` objects. Signals are collected **once** and uploaded in parallel for every account. All uploads are always attempted — a failure for one account does not cancel the others.

```kotlin
try {
  val collection = TrustSignalsSDK.collectAndUpload(
    TSCollectionRequest(accountId = "account-1", accessToken = "token-1", appId = "your-app-id"),
    TSCollectionRequest(accountId = "account-2", accessToken = "token-2", appId = "your-app-id"),
    TSCollectionRequest(accountId = "account-3", accessToken = "token-3", appId = "your-app-id"),
  )
  // All uploads succeeded
} catch (e: TSUploadException) {
  // e.failures contains only the accounts that failed
  e.failures.forEach { (accountId, error) ->
    if (error is TSAuthenticationException) { /* refresh token for accountId */ }
  }
}
```

#### Using callbacks

```kotlin
import com.futurae.sdk.ts.TrustSignalsSDK
import com.futurae.sdk.ts.model.public.TSCollectionRequest
import com.futurae.sdk.ts.error.TSUploadException

TrustSignalsSDK.collectAndUpload(
  TSCollectionRequest(
    accountId = "user-account-id",
    accessToken = "bearer-token",
    appId = "your-app-id",
  ),
  onSuccess = { collection ->
    // Runs on the main thread
  },
  onError = { error ->
    // Runs on the main thread
    if (error is TSUploadException) { /* inspect error.failures */ }
  }
)
```

---

### 3. Schedule Periodic Collections

Use `scheduleCollections` to run automatic collect-and-upload jobs in the background via WorkManager. Jobs survive app restarts.

> **Note:** The minimum interval is **15 minutes** (`TrustSignalsSDK.MIN_COLLECTION_INTERVAL`). Passing a shorter value throws `IllegalArgumentException`.
>
> **Note:** To collect location data during background jobs, the host app must declare and request `ACCESS_BACKGROUND_LOCATION`. See [Background location for scheduled collections](#background-location-for-scheduled-collections).

Each `TSCollectionRequest` produces one **independent** periodic job. Jobs can be stopped individually by account ID, so a failure or cancellation for one account does not affect any other.

```kotlin
import com.futurae.sdk.ts.TrustSignalsSDK
import com.futurae.sdk.ts.model.public.TSCollectionRequest
import kotlin.time.Duration.Companion.minutes

// Single account
TrustSignalsSDK.scheduleCollections(
  interval = 30.minutes,
  TSCollectionRequest(
    accountId = "user-account-id",
    accessToken = "bearer-token",
    appId = "your-app-id",
  )
)

// Multiple accounts — one independent job per account
TrustSignalsSDK.scheduleCollections(
  interval = 30.minutes,
  TSCollectionRequest(accountId = "account-1", accessToken = "token-1", appId = "your-app-id"),
  TSCollectionRequest(accountId = "account-2", accessToken = "token-2", appId = "your-app-id"),
)
```

Calling `scheduleCollections` again for the same account replaces that account's existing schedule without affecting others.

---

### 4. Handle Errors and Stop or Reschedule

Errors from background workers (e.g. expired tokens) are delivered to a registered handler on the main thread.

#### Register an error handler

```kotlin
import com.futurae.sdk.ts.TrustSignalsSDK
import com.futurae.sdk.ts.model.public.TSCollectionRequest
import com.futurae.sdk.ts.error.TSAuthenticationException
import kotlin.time.Duration.Companion.minutes

TrustSignalsSDK.registerErrorHandler { accountId, error ->
  when (error) {
    is TSAuthenticationException -> {
      // Token expired — stop only the failing account and reschedule with a fresh token
      TrustSignalsSDK.stopScheduledCollections(accountId)

      val newToken = refreshAccessToken(accountId)

      TrustSignalsSDK.scheduleCollections(
        interval = 30.minutes,
        TSCollectionRequest(
          accountId = accountId,
          accessToken = newToken,
          appId = "your-app-id",
        )
      )
    }
    else -> {
      // Log or report other errors
    }
  }
}
```

#### Stop scheduled collections

```kotlin
// Stop a specific account's schedule
TrustSignalsSDK.stopScheduledCollections("user-account-id")

// Stop several accounts at once
TrustSignalsSDK.stopScheduledCollections("account-1", "account-2")

// Stop all active schedules
TrustSignalsSDK.stopScheduledCollections()
```

**Error behaviour summary:**

| Context | Error type | Retry | How it surfaces |
|---|---|---|---|
| `collectAndUpload` | HTTP 401 / 403 | No | `TSUploadException` with `TSAuthenticationException` in `failures` |
| `collectAndUpload` | Other HTTP errors | No | `TSUploadException` with `IllegalStateException` in `failures` |
| Scheduled worker | Network / connectivity (`IOException`) | Up to 3 times, then permanent failure | Error handler, after final attempt |
| Scheduled worker | HTTP 401 / 403 | No | Error handler, as `TSAuthenticationException` |
| Scheduled worker | Other HTTP errors | No | Error handler, as `IllegalStateException` |
| Scheduled worker | Unexpected exceptions | No | Error handler |

---

## Sample App

A runnable sample app is available in the [`sample/`](sample/) directory. It demonstrates SDK initialization, manual collect-and-upload (single and multiple accounts), and scheduled background collections using a minimal Jetpack Compose UI.

### Prerequisites

**1. GitHub Packages credentials**

The sample pulls the SDK from GitHub Packages. Add your credentials to `~/.gradle/gradle.properties` (recommended) or to `sample/gradle.properties`:

```properties
GITHUB_ACTOR=YOUR_GITHUB_USERNAME
GITHUB_TOKEN=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

Your token needs the `read:packages` scope. See [Installation](#installation) for details.

**2. Backend URL**

The sample requires a `TS_BASE_URL` Gradle property pointing to your Trust Signals backend. Add it to `~/.gradle/gradle.properties` or `sample/gradle.properties`:

```properties
TS_BASE_URL=https://your-trust-signals-server.example.com
```

---

## Releases

See the [GitHub Releases page](https://github.com/Futurae-Technologies/android-trust-signals-sdk/releases) for the full version history and changelogs.
